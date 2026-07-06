import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { ChamadaPendenteMesaResponse, MesaGarcomResponse } from '../../core/models/garcom.models';
import { AuthService } from '../../core/services/auth';
import { GarcomChamadaService } from '../../core/services/garcom-chamada';
import { GarcomMesaService } from '../../core/services/garcom-mesa';

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
  private readonly garcomMesaService = inject(GarcomMesaService);
  private readonly garcomChamadaService = inject(GarcomChamadaService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly mesas = signal<MesaGarcomResponse[]>([]);
  protected readonly busca = signal('');
  protected readonly filtro = signal<FiltroMesa>('todas');
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
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
          ...mesa.chamadasPendentes.map(chamada => chamada.mensagem),
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

    this.garcomMesaService
      .listarMesas()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: mesas => {
          this.mesas.set(mesas);
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

  protected atenderNotificacao(mesa: MesaGarcomResponse, chamada: ChamadaPendenteMesaResponse): void {
    this.notificacaoEmAcao.set(chamada.id);

    this.garcomChamadaService
      .concluirChamada(chamada.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.notificacaoEmAcao.set(null))
      )
      .subscribe({
        next: () => this.removerChamada(mesa.idMesa, chamada.id),
        error: error => this.erro.set(this.getErrorMessage(error)),
      });
  }

  protected notificacaoDaMesa(mesa: MesaGarcomResponse): ChamadaPendenteMesaResponse | null {
    return mesa.chamadasPendentes[0] ?? null;
  }

  protected statusVisual(mesa: MesaGarcomResponse): 'LIVRE' | 'OCUPADA' | 'CHAMANDO' {
    return mesa.possuiChamadaPendente ? 'CHAMANDO' : mesa.status === 'OCUPADA' ? 'OCUPADA' : 'LIVRE';
  }

  protected tempoAberta(mesa: MesaGarcomResponse): string {
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

  protected pedidosAtivosLabel(mesa: MesaGarcomResponse): string {
    const total = mesa.pedidosAtivos.length;

    return `${total} pedido${total === 1 ? '' : 's'} ativo${total === 1 ? '' : 's'}`;
  }

  protected trackMesa(_: number, mesa: MesaGarcomResponse): number {
    return mesa.idMesa;
  }

  private removerChamada(idMesa: number, idChamada: number): void {
    this.mesas.update(mesas =>
      mesas.map(mesa => {
        if (mesa.idMesa !== idMesa) {
          return mesa;
        }

        const chamadasPendentes = mesa.chamadasPendentes.filter(chamada => chamada.id !== idChamada);

        return {
          ...mesa,
          chamadasPendentes,
          possuiChamadaPendente: chamadasPendentes.length > 0,
        };
      })
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
