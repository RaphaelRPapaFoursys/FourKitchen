import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnDestroy, computed, signal } from '@angular/core';

import { PedidoBalcaoResponse, StatusRetirada } from '../../core/models/retirada.models';
import { RetiradaService } from '../../core/services/retirada.service';
import { Icon } from '../../shared/components/icon/icon';
import { UserMenu } from '../../shared/components/user-menu/user-menu';

@Component({
  selector: 'app-balcao',
  imports: [NgTemplateOutlet, Icon, UserMenu],
  templateUrl: './balcao.html',
  styleUrl: './balcao.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Balcao implements OnDestroy {
  private static readonly INTERVALO_ATUALIZACAO_MS = 5_000;
  private readonly intervalo: ReturnType<typeof setInterval>;
  private carregamentoEmAndamento = false;

  protected readonly pedidos = signal<PedidoBalcaoResponse[]>([]);
  protected readonly carregando = signal(true);
  protected readonly atualizando = signal(false);
  protected readonly pedidoEmAcao = signal<number | null>(null);
  protected readonly erro = signal('');
  protected readonly sucesso = signal('');
  protected readonly sincronizadoEm = signal<Date | null>(null);
  protected readonly recebidos = computed(() => this.porStatus('ENVIADO_COZINHA'));
  protected readonly emPreparo = computed(() => this.porStatus('EM_PREPARO'));
  protected readonly prontos = computed(() => this.porStatus('PRONTO'));
  protected readonly comAtencao = computed(() => this.pedidos().filter(pedido =>
    pedido.status === 'AGUARDANDO_DECISAO' || pedido.status === 'PROBLEMA_COZINHA',
  ));

  constructor(private readonly retiradaService: RetiradaService) {
    this.carregarFila();
    this.intervalo = setInterval(() => this.carregarFila(true), Balcao.INTERVALO_ATUALIZACAO_MS);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalo);
  }

  protected carregarFila(silencioso = false): void {
    if (this.carregamentoEmAndamento) return;

    this.carregamentoEmAndamento = true;
    this.atualizando.set(true);
    if (!silencioso) this.carregando.set(true);
    this.erro.set('');

    this.retiradaService.listarFilaBalcao().subscribe({
      next: pedidos => {
        this.pedidos.set(pedidos);
        this.sincronizadoEm.set(new Date());
        this.finalizarCarregamento();
      },
      error: erro => {
        this.erro.set(this.mensagemErro(erro, 'Nao foi possivel carregar a fila do balcao.'));
        this.finalizarCarregamento();
      },
    });
  }

  protected entregar(pedido: PedidoBalcaoResponse): void {
    if (this.pedidoEmAcao() !== null) return;
    if (!window.confirm(`Confirmar a entrega do pedido #${pedido.codigo}?`)) return;

    this.erro.set('');
    this.sucesso.set('');
    this.pedidoEmAcao.set(pedido.id);
    this.retiradaService.entregar(pedido.id).subscribe({
      next: () => {
        this.pedidos.update(pedidos => pedidos.filter(item => item.id !== pedido.id));
        this.sucesso.set(`Pedido #${pedido.codigo} entregue com sucesso.`);
        this.pedidoEmAcao.set(null);
      },
      error: erro => {
        this.erro.set(this.mensagemErro(erro, 'Nao foi possivel confirmar a entrega.'));
        this.pedidoEmAcao.set(null);
        this.carregarFila(true);
      },
    });
  }

  protected minutosDesde(pedido: PedidoBalcaoResponse): number {
    const referencia = pedido.status === 'PRONTO'
      ? pedido.dataPronto
      : pedido.dataInicioPreparo ?? pedido.dataCriacao;
    const horario = referencia ? new Date(referencia).getTime() : Number.NaN;
    return Number.isNaN(horario) ? 0 : Math.max(0, Math.round((Date.now() - horario) / 60_000));
  }

  protected statusLabel(status: StatusRetirada): string {
    const labels: Record<StatusRetirada, string> = {
      ENVIADO_COZINHA: 'Recebido',
      EM_PREPARO: 'Em preparo',
      PRONTO: 'Pronto',
      AGUARDANDO_DECISAO: 'Aguardando decisao',
      PROBLEMA_COZINHA: 'Problema na cozinha',
    };
    return labels[status];
  }

  protected sincronizadoLabel(): string {
    const data = this.sincronizadoEm();
    return data ? new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(data) : '--:--:--';
  }

  protected limparMensagens(): void {
    this.erro.set('');
    this.sucesso.set('');
  }

  private porStatus(status: StatusRetirada): PedidoBalcaoResponse[] {
    return this.pedidos().filter(pedido => pedido.status === status);
  }

  private finalizarCarregamento(): void {
    this.carregamentoEmAndamento = false;
    this.carregando.set(false);
    this.atualizando.set(false);
  }

  private mensagemErro(erro: unknown, padrao: string): string {
    if (erro instanceof HttpErrorResponse) {
      const corpo = erro.error as { msgError?: string } | null;
      if (corpo?.msgError) return corpo.msgError;
    }
    return padrao;
  }
}
