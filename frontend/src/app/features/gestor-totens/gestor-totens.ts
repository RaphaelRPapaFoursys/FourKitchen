import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { TotemGestorResponse } from '../../core/models/totem-management.models';
import { AtualizarUsuarioGestorRequest, CriarUsuarioGestorRequest } from '../../core/models/user-management.models';
import { AuthService } from '../../core/services/auth';
import { TotemManagementService } from '../../core/services/totem-management.service';
import { UserManagementService } from '../../core/services/user-management.service';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { Sidebar } from '../../shared/components/sidebar/sidebar';

const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
type TotemFilter = 'TODOS' | 'ATENCAO' | 'SEM_ATIVIDADE';

@Component({
  selector: 'app-gestor-totens',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, Sidebar, Topbar, Icon],
  templateUrl: './gestor-totens.html',
  styleUrls: ['../gestor-products/gestor-products.scss', './gestor-totens.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GestorTotens {
  private readonly authService = inject(AuthService);
  private readonly totemService = inject(TotemManagementService);
  private readonly userService = inject(UserManagementService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly totems = signal<TotemGestorResponse[]>([]);
  protected readonly searchTerm = signal('');
  protected readonly filter = signal<TotemFilter>('TODOS');
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly actionTotemId = signal<number | null>(null);
  protected readonly dialogOpen = signal(false);
  protected readonly editingTotem = signal<TotemGestorResponse | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');

  protected readonly enabledCount = computed(() => this.totems().filter(totem => totem.ativo).length);
  protected readonly ordersToday = computed(() => this.totems().reduce((total, totem) => total + totem.pedidosHoje, 0));
  protected readonly salesToday = computed(() => this.totems().reduce((total, totem) => total + totem.valorHoje, 0));
  protected readonly attentionCount = computed(() => this.totems().filter(totem => totem.problemasAbertos > 0).length);
  protected readonly filteredTotems = computed(() => {
    const term = this.normalizeText(this.searchTerm());
    return this.totems().filter(totem => {
      const matchesTerm = !term || this.normalizeText(`${totem.nome} ${totem.email} ${totem.id}`).includes(term);
      const matchesFilter = this.filter() === 'TODOS'
        || (this.filter() === 'ATENCAO' && totem.problemasAbertos > 0)
        || (this.filter() === 'SEM_ATIVIDADE' && !totem.ultimaAtividade);
      return matchesTerm && matchesFilter;
    });
  });

  protected readonly totemForm = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3), Validators.maxLength(120)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    senha: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(STRONG_PASSWORD)] }),
  });

  constructor() {
    this.loadTotems();
  }

  protected setFilter(filter: TotemFilter): void {
    this.filter.set(filter);
  }

  protected openCreateDialog(): void {
    this.clearMessages();
    this.editingTotem.set(null);
    this.totemForm.controls.senha.setValidators([Validators.required, Validators.pattern(STRONG_PASSWORD)]);
    this.totemForm.reset({ nome: '', email: '', senha: '' });
    this.dialogOpen.set(true);
  }

  protected openEditDialog(totem: TotemGestorResponse): void {
    this.clearMessages();
    this.editingTotem.set(totem);
    this.totemForm.controls.senha.setValidators([Validators.pattern(STRONG_PASSWORD)]);
    this.totemForm.reset({ nome: totem.nome, email: totem.email, senha: '' });
    this.dialogOpen.set(true);
  }

  protected closeDialog(): void {
    if (!this.saving()) {
      this.dialogOpen.set(false);
      this.editingTotem.set(null);
    }
  }

  protected saveTotem(): void {
    this.clearMessages();
    this.totemForm.markAllAsTouched();
    if (this.totemForm.invalid || this.saving()) return;

    const value = this.totemForm.getRawValue();
    const current = this.editingTotem();
    const common = {
      nome: value.nome.trim(),
      email: value.email.trim().toLowerCase(),
      perfilUsuario: 'TOTEM' as const,
      idMesa: null,
    };
    const operation = current
      ? this.userService.updateUser(current.id, { ...common, senha: value.senha.trim() || null } satisfies AtualizarUsuarioGestorRequest)
      : this.userService.createUser({ ...common, senha: value.senha } satisfies CriarUsuarioGestorRequest);

    this.saving.set(true);
    operation.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))).subscribe({
      next: user => {
        const previous = current ? this.totems().find(totem => totem.id === current.id) : null;
        const updated: TotemGestorResponse = {
          id: user.id,
          nome: user.nome,
          email: user.email,
          ativo: user.ativo,
          pedidosHoje: previous?.pedidosHoje ?? 0,
          valorHoje: previous?.valorHoje ?? 0,
          ultimaAtividade: previous?.ultimaAtividade ?? null,
          problemasAbertos: previous?.problemasAbertos ?? 0,
        };
        this.totems.update(totems => this.sortTotems([...totems.filter(item => item.id !== updated.id), updated]));
        this.dialogOpen.set(false);
        this.editingTotem.set(null);
        this.successMessage.set(current ? 'Totem atualizado com sucesso.' : 'Totem cadastrado com sucesso.');
      },
      error: error => this.errorMessage.set(this.getErrorMessage(error)),
    });
  }

  protected deactivateTotem(totem: TotemGestorResponse): void {
    if (this.actionTotemId() !== null || !window.confirm(`Deseja inativar o acesso do ${totem.nome}?`)) return;

    this.clearMessages();
    this.actionTotemId.set(totem.id);
    this.userService.deactivateUser(totem.id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.actionTotemId.set(null)))
      .subscribe({
        next: () => {
          this.totems.update(totems => totems.filter(item => item.id !== totem.id));
          this.successMessage.set('Totem inativado com sucesso.');
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected refresh(): void {
    this.loadTotems();
  }

  protected initials(name: string | null | undefined): string {
    const parts = name?.trim().split(/\s+/).filter(Boolean) ?? [];
    return parts.slice(0, 2).map(part => part.charAt(0).toUpperCase()).join('') || 'T';
  }

  private loadTotems(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.totemService.listTotems()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false)))
      .subscribe({
        next: totems => this.totems.set(this.sortTotems(totems)),
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  private sortTotems(totems: TotemGestorResponse[]): TotemGestorResponse[] {
    return [...totems].sort((left, right) => left.nome.localeCompare(right.nome, 'pt-BR'));
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private normalizeText(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { msgError?: unknown } | null;
      if (typeof body?.msgError === 'string' && body.msgError.trim()) return body.msgError;
      if (error.status === 401) return 'Sua sessão expirou. Entre novamente.';
      if (error.status === 403) return 'Você não tem permissão para gerenciar totens.';
      if (error.status === 409) return 'Este e-mail já está cadastrado.';
      if (error.status === 502) return 'Um serviço necessário está indisponível. Tente novamente.';
    }
    return 'Não foi possível concluir a operação. Tente novamente.';
  }
}
