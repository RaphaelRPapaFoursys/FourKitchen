import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, forkJoin, of } from 'rxjs';

import { MesaResponse } from '../../core/models/mesa.models';
import { NotificacaoResponse } from '../../core/models/notificacao.models';
import { AuthService } from '../../core/services/auth';
import { MesaService } from '../../core/services/mesa';
import { NotificacaoService } from '../../core/services/notificacao';

type FiltroMesa = 'todas' | 'ocupadas' | 'livres';

@Component({
  selector: 'app-garcom',
  standalone: true,
  imports: [],
  templateUrl: './garcom.html',
  styleUrl: './garcom.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Garcom {
  private readonly mesaService = inject(MesaService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly mesas = signal<MesaResponse[]>([]);
  protected readonly notificacoes = signal<NotificacaoResponse[]>([]);
  protected readonly busca = signal('');
  protected readonly filtro = signal<FiltroMesa>('todas');
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
  protected readonly mesaEmAcao = signal<number | null>(null);
  protected readonly notificacaoEmAcao = signal<number | null>(null);

  protected readonly nomeUsuario = computed(() =>
    this.authService.getCurrentUser()?.nome ?? 'Joao Silva'
  );

  protected readonly mesasFiltradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();

    return this.mesas()
      .filter(mesa => {
        if (this.filtro() === 'ocupadas' && mesa.status !== 'OCUPADA') {
          return false;
        }

        if (this.filtro() === 'livres' && mesa.status !== 'DISPONIVEL') {
          return false;
        }

        if (!termo) {
          return true;
        }

        return [
          mesa.numero,
          mesa.status,
          this.statusVisual(mesa),
          this.notificacaoDaMesa(mesa)?.mensagem,
        ].join(' ').toLowerCase().includes(termo);
      })
      .sort((a, b) => a.numero - b.numero);
  });

  protected readonly totalMesas = computed(() => this.mesas().length);
  protected readonly mesasOcupadas = computed(() => this.mesas().filter(mesa => mesa.status === 'OCUPADA').length);
  protected readonly mesasLivres = computed(() => this.mesas().filter(mesa => mesa.status === 'DISPONIVEL').length);

  constructor() {
    this.carregarDashboard();
  }

  protected carregarDashboard(): void {
    this.erro.set('');
    this.carregando.set(true);

    forkJoin({
      mesas: this.mesaService.listarMesas(),
      notificacoes: this.notificacaoService.listarPendentes('GARCOM').pipe(catchError(() => of([]))),
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: ({ mesas, notificacoes }) => {
          this.mesas.set(mesas);
          this.notificacoes.set(notificacoes);
        },
        error: error => this.erro.set(this.getErrorMessage(error)),
      });
  }

  protected atualizarBusca(event: Event): void {
    this.busca.set((event.target as HTMLInputElement).value);
  }

  protected alterarFiltro(filtro: FiltroMesa): void {
    this.filtro.set(filtro);
  }

  protected abrirMesa(mesa: MesaResponse): void {
    this.executarAcaoMesa(mesa.id, () => this.mesaService.abrirMesa(mesa.id));
  }

  protected fecharMesa(mesa: MesaResponse): void {
    this.executarAcaoMesa(mesa.id, () => this.mesaService.fecharMesa(mesa.id));
  }

  protected atenderNotificacao(notificacao: NotificacaoResponse): void {
    this.notificacaoEmAcao.set(notificacao.id);

    this.notificacaoService
      .marcarComoLida(notificacao.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.notificacaoEmAcao.set(null))
      )
      .subscribe({
        next: notificacaoLida => {
          this.notificacoes.update(notificacoes =>
            notificacoes.filter(notificacaoAtual => notificacaoAtual.id !== notificacaoLida.id)
          );
        },
        error: error => this.erro.set(this.getErrorMessage(error)),
      });
  }

  protected notificacaoDaMesa(mesa: MesaResponse): NotificacaoResponse | null {
    const numeroMesa = mesa.numero.toString().padStart(2, '0');

    return this.notificacoes().find(notificacao => {
      const texto = `${notificacao.tipo} ${notificacao.mensagem}`.toLowerCase();

      return texto.includes(`mesa ${mesa.numero}`) || texto.includes(`mesa ${numeroMesa}`);
    }) ?? null;
  }

  protected statusVisual(mesa: MesaResponse): 'LIVRE' | 'OCUPADA' | 'CHAMANDO' {
    return this.notificacaoDaMesa(mesa) ? 'CHAMANDO' : mesa.status === 'OCUPADA' ? 'OCUPADA' : 'LIVRE';
  }

  protected tempoAberta(mesa: MesaResponse): string {
    if (!mesa.dataAbertura) {
      return '';
    }

    const dataAbertura = new Date(mesa.dataAbertura).getTime();

    if (Number.isNaN(dataAbertura)) {
      return '';
    }

    const minutos = Math.max(0, Math.round((Date.now() - dataAbertura) / 60000));

    return `${minutos} min`;
  }

  protected trackMesa(_: number, mesa: MesaResponse): number {
    return mesa.id;
  }

  private executarAcaoMesa(mesaId: number, requestFactory: () => ReturnType<MesaService['abrirMesa']>): void {
    this.erro.set('');
    this.mesaEmAcao.set(mesaId);

    requestFactory()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.mesaEmAcao.set(null))
      )
      .subscribe({
        next: mesaAtualizada => this.substituirMesa(mesaAtualizada),
        error: error => this.erro.set(this.getErrorMessage(error)),
      });
  }

  private substituirMesa(mesaAtualizada: MesaResponse): void {
    this.mesas.update(mesas =>
      mesas.map(mesa => mesa.id === mesaAtualizada.id ? mesaAtualizada : mesa)
    );
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
        return 'Voce nao tem permissao para acessar o dashboard do garcom.';
      }

      if (error.status === 502) {
        return 'Servico de mesas indisponivel. Tente novamente mais tarde.';
      }
    }

    return 'Nao foi possivel carregar o dashboard do garcom.';
  }

  private getApiError(error: unknown): { msgError: string } | null {
    if (typeof error !== 'object' || error === null || !('msgError' in error)) {
      return null;
    }

    const msgError = error['msgError'];

    return typeof msgError === 'string' ? { msgError } : null;
  }
}
