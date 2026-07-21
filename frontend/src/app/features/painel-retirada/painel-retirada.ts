import { ChangeDetectionStrategy, Component, OnDestroy, computed, signal } from '@angular/core';

import { PedidoPainelRetiradaResponse } from '../../core/models/retirada.models';
import { RetiradaService } from '../../core/services/retirada.service';

@Component({
  selector: 'app-painel-retirada',
  templateUrl: './painel-retirada.html',
  styleUrl: './painel-retirada.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PainelRetirada implements OnDestroy {
  private static readonly INTERVALO_ATUALIZACAO_MS = 5_000;
  private readonly intervalo: ReturnType<typeof setInterval>;
  private carregamentoEmAndamento = false;

  protected readonly pedidos = signal<PedidoPainelRetiradaResponse[]>([]);
  protected readonly carregando = signal(true);
  protected readonly indisponivel = signal(false);
  protected readonly atualizadoEm = signal<Date | null>(null);
  protected readonly emPreparo = computed(() =>
    this.pedidos().filter(pedido => pedido.status !== 'PRONTO'),
  );
  protected readonly prontos = computed(() =>
    this.pedidos().filter(pedido => pedido.status === 'PRONTO'),
  );

  constructor(private readonly retiradaService: RetiradaService) {
    this.atualizar();
    this.intervalo = setInterval(() => this.atualizar(true), PainelRetirada.INTERVALO_ATUALIZACAO_MS);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalo);
  }

  protected atualizar(silencioso = false): void {
    if (this.carregamentoEmAndamento) return;

    this.carregamentoEmAndamento = true;
    if (!silencioso) this.carregando.set(true);

    this.retiradaService.listarPainelPublico().subscribe({
      next: pedidos => {
        this.pedidos.set(pedidos);
        this.atualizadoEm.set(new Date());
        this.indisponivel.set(false);
        this.finalizarCarregamento();
      },
      error: () => {
        this.indisponivel.set(true);
        this.finalizarCarregamento();
      },
    });
  }

  protected horarioAtualizacao(): string {
    const data = this.atualizadoEm();
    return data
      ? new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(data)
      : '--:--:--';
  }

  private finalizarCarregamento(): void {
    this.carregamentoEmAndamento = false;
    this.carregando.set(false);
  }
}
