import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/services/auth';

interface MesaResumo {
  id: number;
  numero: number;
  status: 'OCUPADA' | 'DISPONIVEL';
}

interface PedidoMesaAtivo {
  id: number;
  item: string;
  status: 'Em preparo' | 'Pronto';
  valor: number;
  destaque: 'preparo' | 'pronto';
}

const pedidosAtivos: PedidoMesaAtivo[] = [
  {
    id: 104,
    item: 'Burger, aries',
    status: 'Em preparo',
    valor: 54.90,
    destaque: 'preparo',
  },
  {
    id: 105,
    item: 'Pizza, ouice',
    status: 'Pronto',
    valor: 82.00,
    destaque: 'pronto',
  },
];

@Component({
  selector: 'app-mesa',
  imports: [],
  templateUrl: './mesa.html',
  styleUrl: './mesa.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Mesa {
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly mesaAtiva = signal<MesaResumo | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
  protected readonly pedidosAtivos = signal<PedidoMesaAtivo[]>(pedidosAtivos);

  protected readonly mesaNumero = computed(() =>
    this.mesaAtiva()?.numero ?? 3
  );

  protected readonly mesaStatus = computed(() =>
    this.mesaAtiva()?.status ?? 'OCUPADA'
  );

  protected readonly pedidosEncontrados = computed(() =>
    this.pedidosAtivos().length
  );

  protected readonly nomeUsuario = computed(() =>
    this.authService.getCurrentUser()?.nome ?? 'Joao Silva'
  );

  constructor() {
    this.carregarMesaAutenticada();
  }

  protected carregarMesaAutenticada(): void {
    this.erro.set('');
    this.carregando.set(true);

    this.authService
      .me()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: usuario => {
          if (!usuario.idMesa) {
            this.erro.set('Mesa nao vinculada ao usuario autenticado.');
            this.mesaAtiva.set(null);
            return;
          }

          this.mesaAtiva.set({
            id: usuario.idMesa,
            numero: usuario.idMesa,
            status: 'OCUPADA',
          });
        },
        error: error => this.erro.set(this.getErrorMessage(error)),
      });
  }

  protected fecharConta(): void {
    this.erro.set('Fechamento de conta pela mesa depende de endpoint especifico no BFF.');
  }

  protected novoPedido(): void {
    this.erro.set('');
  }

  protected voltarDashboard(): void {
    history.back();
  }

  protected valorPedido(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(valor);
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const apiError = this.getApiError(error.error);

      if (apiError?.msgError) {
        return apiError.msgError;
      }

      if (error.status === 401) {
        return 'Sessao expirada. Faca login novamente.';
      }

      if (error.status === 403) {
        return 'Voce nao tem permissao para executar esta acao.';
      }

      if (error.status === 502) {
        return 'Servico de mesas indisponivel. Tente novamente mais tarde.';
      }
    }

    return 'Nao foi possivel concluir a operacao.';
  }

  private getApiError(error: unknown): { msgError: string } | null {
    if (typeof error !== 'object' || error === null || !('msgError' in error)) {
      return null;
    }

    const msgError = error['msgError'];

    return typeof msgError === 'string' ? { msgError } : null;
  }
}
