import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, of, switchMap, timer } from 'rxjs';

import { MesaAtendimentoAtualResponse } from '../../../core/models/order.models';
import { AuthService } from '../../../core/services/auth';
import { MesaChamadaService } from '../../../core/services/mesa-chamada';
import { OrderService } from '../../../core/services/order.service';
import { Icon } from '../icon/icon';

export type MesaHeaderActiveLink = 'orders' | 'cart' | null;
type MesaToastType = 'success' | 'warning' | 'error';
interface MesaToast {
  message: string;
  type: MesaToastType;
}

@Component({
  selector: 'app-mesa-header',
  imports: [Icon],
  templateUrl: './mesa-header.html',
  styleUrl: './mesa-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MesaHeaderComponent implements OnInit {
  readonly homeRoute = input.required<string>();
  readonly cartRoute = input.required<string>();
  readonly ordersRoute = input.required<string>();
  readonly showOrdersLink = input(true);
  readonly activeLink = input<MesaHeaderActiveLink>(null);
  readonly totalItems = input(0);
  readonly atendimento = input<MesaAtendimentoAtualResponse | null | undefined>(undefined);
  readonly atendimentoLoading = input(false);
  readonly attendanceError = input('');

  readonly menuSelected = output<Event>();
  readonly cartSelected = output<Event>();
  readonly ordersSelected = output<Event>();

  private readonly authService = inject(AuthService);
  private readonly mesaChamadaService = inject(MesaChamadaService);
  private readonly orderService = inject(OrderService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly atendimentoInterno = signal<MesaAtendimentoAtualResponse | null>(null);
  private readonly carregandoAtendimentoInterno = signal(true);
  private toastTimeoutId: number | null = null;
  protected readonly chamandoGarcom = signal(false);
  protected readonly chamadaEnviada = computed(() => {
    const codigoAtendimento = this.atendimentoAtual()?.codigoAtendimento;
    return codigoAtendimento !== undefined
      && this.mesaChamadaService.chamadaAtendimentoCodigoAtual() === codigoAtendimento;
  });
  protected readonly toast = signal<MesaToast | null>(null);

  protected readonly mesaNumero = computed(() =>
    this.authService.getCurrentUser()?.idMesa ?? this.atendimentoAtual()?.idMesa ?? null,
  );

  protected readonly mesaLabel = computed(() => {
    const numero = this.mesaNumero();
    return numero === null ? 'Mesa —' : `Mesa ${String(numero).padStart(2, '0')}`;
  });

  protected readonly atendimentoAtivo = computed(() =>
    this.isAtendimentoAtivo(this.atendimentoAtual()),
  );

  protected readonly atendimentoAtual = computed(() =>
    this.atendimento() === undefined ? this.atendimentoInterno() : this.atendimento() ?? null,
  );

  protected readonly carregandoAtendimento = computed(() =>
    this.atendimento() === undefined ? this.carregandoAtendimentoInterno() : this.atendimentoLoading(),
  );

  protected readonly chamadaDesabilitada = computed(() =>
    this.carregandoAtendimento()
      || !this.atendimentoAtivo()
      || this.chamandoGarcom()
      || this.chamadaEnviada(),
  );

  protected readonly chamadaLabel = computed(() => {
    if (this.chamandoGarcom()) {
      return 'Chamando garçom';
    }

    if (this.chamadaEnviada()) {
      return 'Garçom a caminho';
    }

    return 'Chamar garçom';
  });

  constructor() {
    effect(onCleanup => {
      const atendimento = this.atendimentoAtual();
      if (!atendimento || !this.chamadaEnviada()) {
        return;
      }

      const subscription = timer(0, 10_000)
        .pipe(
          switchMap(() => this.mesaChamadaService
            .sincronizarChamadaPendente(atendimento.codigoAtendimento)
            .pipe(catchError(() => of(true)))),
        )
        .subscribe();

      onCleanup(() => subscription.unsubscribe());
    });

    effect(() => {
      if (this.atendimento() === undefined || this.carregandoAtendimento()) {
        return;
      }

      if (this.attendanceError()) {
        this.showToast(
          'Não conseguimos verificar o atendimento da mesa agora. Tente novamente em instantes.',
          'error',
        );
      } else if (!this.atendimentoAtivo()) {
        this.showToast('O atendimento desta mesa ainda não foi iniciado.', 'warning');
      }
    });

    this.destroyRef.onDestroy(() => this.clearToastTimeout());
  }

  ngOnInit(): void {
    if (this.atendimento() === undefined) {
      this.carregarAtendimento();
    }
  }

  protected selecionarMenu(event: Event): void {
    this.menuSelected.emit(event);
  }

  protected selecionarCarrinho(event: Event): void {
    this.cartSelected.emit(event);
  }

  protected selecionarPedidos(event: Event): void {
    this.ordersSelected.emit(event);
  }

  protected chamarGarcom(): void {
    const atendimento = this.atendimentoAtual();

    if (!this.isAtendimentoAtivo(atendimento) || this.chamandoGarcom() || this.chamadaEnviada()) {
      return;
    }

    this.chamandoGarcom.set(true);
    this.closeToast();
    this.mesaChamadaService
      .chamarGarcom(atendimento.codigoAtendimento)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.chamandoGarcom.set(false)),
      )
      .subscribe({
        next: response => {
          this.mesaChamadaService.marcarChamadaEmAndamento(
            atendimento.codigoAtendimento,
            response.id,
          );
          this.showToast('Garçom avisado. Ele está a caminho.', 'success');
        },
        error: error => this.showToast(this.getFriendlyErrorMessage(error), 'error'),
      });
  }

  protected isActive(link: MesaHeaderActiveLink): boolean {
    return this.activeLink() === link;
  }

  private carregarAtendimento(): void {
    this.orderService
      .getCurrentTableAttendance()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregandoAtendimentoInterno.set(false)),
      )
      .subscribe({
        next: atendimento => {
          this.atendimentoInterno.set(this.isAtendimentoAtivo(atendimento) ? atendimento : null);
          if (!this.isAtendimentoAtivo(atendimento)) {
            this.showToast('O atendimento desta mesa ainda não foi iniciado.', 'warning');
          }
        },
        error: error => {
          this.atendimentoInterno.set(null);
          this.showToast(
            error instanceof HttpErrorResponse && error.status === 400
              ? 'O atendimento desta mesa ainda não foi iniciado.'
              : this.getFriendlyErrorMessage(error),
            error instanceof HttpErrorResponse && error.status === 400 ? 'warning' : 'error',
          );
        },
      });
  }

  private isAtendimentoAtivo(atendimento: MesaAtendimentoAtualResponse | null): atendimento is MesaAtendimentoAtualResponse {
    return atendimento !== null
      && atendimento.status.toUpperCase() === 'OCUPADA'
      && atendimento.idAtendimento > 0
      && atendimento.codigoAtendimento > 0;
  }

  protected closeToast(): void {
    this.clearToastTimeout();
    this.toast.set(null);
  }

  private showToast(message: string, type: MesaToastType): void {
    this.clearToastTimeout();
    this.toast.set({ message, type });
    this.toastTimeoutId = window.setTimeout(() => {
      this.toast.set(null);
      this.toastTimeoutId = null;
    }, 4_500);
  }

  private clearToastTimeout(): void {
    if (this.toastTimeoutId !== null) {
      window.clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }
  }

  private getFriendlyErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === 401) {
      return 'Sua sessão terminou. Entre novamente para continuar.';
    }

    if (error instanceof HttpErrorResponse && error.status === 403) {
      return 'Esta mesa não pode chamar o garçom no momento.';
    }

    return 'Não conseguimos chamar o garçom agora. Tente novamente em instantes.';
  }
}
