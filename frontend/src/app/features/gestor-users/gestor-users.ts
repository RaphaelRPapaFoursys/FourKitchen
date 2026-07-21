import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import {
  AtualizarUsuarioGestorRequest,
  CriarUsuarioGestorRequest,
  PerfilUsuario,
  UsuarioGestorResponse,
} from '../../core/models/user-management.models';
import { AuthService } from '../../core/services/auth';
import { UserManagementService } from '../../core/services/user-management.service';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { Sidebar } from '../../shared/components/sidebar/sidebar';

const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

interface ProfileOption {
  value: PerfilUsuario;
  label: string;
}

@Component({
  selector: 'app-gestor-users',
  imports: [ReactiveFormsModule, Sidebar, Topbar, Icon],
  templateUrl: './gestor-users.html',
  styleUrls: ['../gestor-products/gestor-products.scss', './gestor-users.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GestorUsers {
  private readonly authService = inject(AuthService);
  private readonly userManagementService = inject(UserManagementService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly users = signal<UsuarioGestorResponse[]>([]);
  protected readonly searchTerm = signal('');
  protected readonly profileFilter = signal<PerfilUsuario | null>(null);
  protected readonly statusFilter = signal<boolean | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly actionUserId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly dialogOpen = signal(false);
  protected readonly editingUser = signal<UsuarioGestorResponse | null>(null);

  protected readonly profileOptions: ProfileOption[] = [
    { value: 'GESTOR', label: 'Gestor' },
    { value: 'GARCOM', label: 'Garçom' },
    { value: 'COZINHA', label: 'Cozinha' },
    { value: 'BALCAO', label: 'Balcao' },
    { value: 'MESA', label: 'Mesa' },
    { value: 'TOTEM', label: 'Totem' },
    { value: 'ADMIN', label: 'Administrador' },
  ];

  protected readonly userForm = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(120)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(STRONG_PASSWORD)],
    }),
    perfilUsuario: new FormControl<PerfilUsuario | null>(null, {
      validators: [Validators.required],
    }),
    idMesa: new FormControl<number | null>(null),
  });

  protected readonly selectedProfile = toSignal(this.userForm.controls.perfilUsuario.valueChanges, {
    initialValue: this.userForm.controls.perfilUsuario.value,
  });
  protected readonly passwordValue = toSignal(this.userForm.controls.senha.valueChanges, {
    initialValue: this.userForm.controls.senha.value,
  });
  protected readonly isMesaProfile = computed(() => this.selectedProfile() === 'MESA');
  protected readonly passwordRequirements = computed(() => {
    const password = this.passwordValue();
    return [
      { label: 'Pelo menos 8 caracteres', met: password.length >= 8 },
      { label: 'Uma letra maiúscula', met: /[A-Z]/.test(password) },
      { label: 'Uma letra minúscula', met: /[a-z]/.test(password) },
      { label: 'Um número', met: /\d/.test(password) },
    ];
  });
  protected readonly filteredUsers = computed(() => {
    const term = this.normalizeText(this.searchTerm());
    const profile = this.profileFilter();
    const status = this.statusFilter();
    return this.users().filter(user => {
      const matchesProfile = profile === null || user.perfilUsuario === profile;
      const matchesStatus = status === null || user.ativo === status;
      const matchesTerm = !term || this.normalizeText(
        `${user.nome} ${user.email} ${this.profileLabel(user.perfilUsuario)}`,
      ).includes(term);
      return matchesProfile && matchesStatus && matchesTerm;
    });
  });

  constructor() {
    this.loadUsers();
  }

  protected openCreateDialog(): void {
    this.clearMessages();
    this.editingUser.set(null);
    this.userForm.controls.senha.setValidators([
      Validators.required,
      Validators.pattern(STRONG_PASSWORD),
    ]);
    this.userForm.reset({
      nome: '',
      email: '',
      senha: '',
      perfilUsuario: null,
      idMesa: null,
    });
    this.updateMesaValidation();
    this.dialogOpen.set(true);
  }

  protected openEditDialog(user: UsuarioGestorResponse): void {
    this.clearMessages();
    this.editingUser.set(user);
    this.userForm.controls.senha.setValidators([Validators.pattern(STRONG_PASSWORD)]);
    this.userForm.reset({
      nome: user.nome,
      email: user.email,
      senha: '',
      perfilUsuario: user.perfilUsuario,
      idMesa: user.idMesa,
    });
    this.updateMesaValidation();
    this.dialogOpen.set(true);
  }

  protected selectProfileFilter(profile: PerfilUsuario | null): void {
    this.profileFilter.set(profile);
  }

  protected selectStatusFilter(status: boolean | null): void {
    this.statusFilter.set(status);
  }

  protected closeDialog(): void {
    if (this.saving()) {
      return;
    }

    this.dialogOpen.set(false);
    this.editingUser.set(null);
  }

  protected onProfileChanged(): void {
    this.updateMesaValidation();
  }

  protected saveUser(): void {
    this.clearMessages();
    this.updateMesaValidation();
    this.userForm.markAllAsTouched();

    if (this.userForm.invalid || this.saving()) {
      return;
    }

    const formValue = this.userForm.getRawValue();
    if (!formValue.perfilUsuario) {
      return;
    }

    const currentUser = this.editingUser();
    const commonRequest = {
      nome: this.normalizeName(formValue.nome),
      email: formValue.email.trim().toLowerCase(),
      perfilUsuario: formValue.perfilUsuario,
      idMesa: formValue.perfilUsuario === 'MESA' ? formValue.idMesa : null,
    };
    const operation = currentUser
      ? this.userManagementService.updateUser(currentUser.id, {
          ...commonRequest,
          senha: formValue.senha.trim() || null,
        } satisfies AtualizarUsuarioGestorRequest)
      : this.userManagementService.createUser({
          ...commonRequest,
          senha: formValue.senha,
        } satisfies CriarUsuarioGestorRequest);

    this.saving.set(true);
    operation
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: savedUser => {
          this.upsertUser(savedUser);
          this.dialogOpen.set(false);
          this.editingUser.set(null);
          this.successMessage.set(
            currentUser ? 'Usuário atualizado com sucesso.' : 'Usuário cadastrado com sucesso.',
          );
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected deactivateUser(user: UsuarioGestorResponse): void {
    if (this.actionUserId() !== null) {
      return;
    }

    if (this.isCurrentUser(user)) {
      this.errorMessage.set('Você não pode inativar o próprio usuário.');
      return;
    }

    if (!window.confirm(`Deseja inativar o usuário ${user.nome}?`)) {
      return;
    }

    this.clearMessages();
    this.actionUserId.set(user.id);
    this.userManagementService
      .deactivateUser(user.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.actionUserId.set(null)),
      )
      .subscribe({
        next: () => {
          this.users.update(users =>
            users.map(item => item.id === user.id ? { ...item, ativo: false } : item),
          );
          this.successMessage.set('Usuário inativado com sucesso.');
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected profileLabel(profile: PerfilUsuario): string {
    return this.profileOptions.find(option => option.value === profile)?.label ?? profile;
  }

  protected isCurrentUser(user: UsuarioGestorResponse): boolean {
    return this.usuario()?.id === user.id;
  }

  protected trackUser(_index: number, user: UsuarioGestorResponse): number {
    return user.id;
  }

  protected initials(name: string | null | undefined): string {
    const parts = name?.trim().split(/\s+/).filter(Boolean) ?? [];
    return parts.slice(0, 2).map(part => part.charAt(0).toUpperCase()).join('') || '?';
  }

  protected logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.userManagementService
      .listUsers()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: users => this.users.set(this.sortUsers(users)),
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  private updateMesaValidation(): void {
    const control = this.userForm.controls.idMesa;
    if (this.userForm.controls.perfilUsuario.value === 'MESA') {
      control.setValidators([Validators.required, Validators.min(1)]);
    } else {
      control.clearValidators();
      control.setValue(null, { emitEvent: false });
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  private upsertUser(user: UsuarioGestorResponse): void {
    this.users.update(users =>
      this.sortUsers([...users.filter(item => item.id !== user.id), user]),
    );
  }

  private sortUsers(users: UsuarioGestorResponse[]): UsuarioGestorResponse[] {
    return [...users].sort((left, right) => left.nome.localeCompare(right.nome, 'pt-BR'));
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  protected activateUser(user: UsuarioGestorResponse): void {
    if (this.actionUserId() !== null || user.ativo) {
      return;
    }

    this.clearMessages();
    this.actionUserId.set(user.id);
    this.userManagementService
      .activateUser(user.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.actionUserId.set(null)),
      )
      .subscribe({
        next: activatedUser => {
          this.upsertUser(activatedUser);
          this.successMessage.set('Usuário ativado com sucesso.');
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected toggleUserStatus(user: UsuarioGestorResponse): void {
    if (user.ativo) {
      this.deactivateUser(user);
      return;
    }

    this.activateUser(user);
  }

  private normalizeName(value: string): string {
    const normalized = value.trim().replace(/\s+/g, ' ').toLocaleLowerCase('pt-BR');
    return normalized.charAt(0).toLocaleUpperCase('pt-BR') + normalized.slice(1);
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { msgError?: unknown } | null;
      if (typeof body?.msgError === 'string' && body.msgError.trim()) {
        return body.msgError;
      }

      if (error.status === 400) return 'Confira os dados, a senha e o vínculo da mesa.';
      if (error.status === 401) return 'Sua sessão expirou. Entre novamente.';
      if (error.status === 403) return 'Você não tem permissão para gerenciar usuários.';
      if (error.status === 404) return 'Usuário não encontrado.';
      if (error.status === 409) return 'Este e-mail já está cadastrado.';
      if (error.status === 502) return 'O serviço de usuários está indisponível. Tente novamente.';
    }

    return 'Não foi possível concluir a operação. Tente novamente.';
  }
}
