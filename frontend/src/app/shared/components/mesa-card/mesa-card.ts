import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { Criticidade } from '../../../core/constants/urgencia.constants';
import { AcaoMesaPainel, MesaPainel } from '../../../core/models/painel.models';
import { Badge } from '../badge/badge';
import { Icon } from '../icon/icon';
import { ProgressBar } from '../progress-bar/progress-bar';

const CHAVES_ETAPAS: Record<number, string> = {
  1: 'TABLE.STAGE_SENT',
  2: 'TABLE.STAGE_PREPARATION',
  3: 'TABLE.STAGE_FINISHING',
  4: 'TABLE.STAGE_READY',
};

/**
 * Card de mesa do painel do gestor. Apresentacional: recebe a mesa e as derivações
 * (criticidade, ação primária, bloqueio) prontas; emite `acao`/`trocar`/`verPedido`.
 * O :host é o próprio card e carrega o `id="mesa-card-N"` usado como âncora de destaque.
 */
@Component({
  selector: 'fk-mesa-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CurrencyPipe, Badge, Icon, ProgressBar, TranslatePipe],
  host: {
    class: 'mesa-card',
    '[id]': "'mesa-card-' + mesa().numero",
    '[class.mesa-card--destacada]': 'destacada()',
  },
  templateUrl: './mesa-card.html',
  styleUrl: './mesa-card.scss',
})
export class MesaCard {
  private readonly translate = inject(TranslateService);
  readonly mesa = input.required<MesaPainel>();
  readonly criticidade = input.required<Criticidade>();
  readonly acaoPrimaria = input.required<{ tipo: AcaoMesaPainel; label: string }>();
  /** Expediente fechado ou ação em andamento: desabilita interações de mutação. */
  readonly bloqueado = input(false);
  readonly destacada = input(false);

  readonly acao = output<void>();
  readonly trocar = output<void>();
  readonly verPedido = output<void>();

  protected readonly urgenciaBadgeLabel = computed<string | null>(() => {
    const criticidade = this.criticidade();
    if (criticidade === 'critico') return this.translate.instant('TABLE.LATE');
    if (criticidade === 'atencao') return this.translate.instant('TABLE.ATTENTION');
    return null;
  });

  protected readonly temAcaoSecundaria = computed(
    () =>
      this.mesa().status === 'OCUPADA' &&
      this.acaoPrimaria().tipo !== 'VER_PEDIDO' &&
      (this.criticidade() === 'critico' || this.criticidade() === 'atencao'),
  );

  protected readonly acaoPrimariaIndisponivel = computed(
    () => this.bloqueado() || this.acaoPrimaria().tipo === 'VER_PEDIDO',
  );

  protected readonly valorConta = computed<number | null>(() => {
    const pedidos = this.mesa().pedidos;
    if (pedidos.length === 0) return null;
    return pedidos.reduce((total, pedido) => total + pedido.valor, 0);
  });

  protected readonly itensResumo = computed<string>(() => {
    const pedidos = this.mesa().pedidos;
    if (pedidos.length === 0) return '';
    const totalItens = pedidos.reduce((total, pedido) => total + pedido.totalItens, 0);
    return this.translate.instant('COMMON.ITEMS', { count: totalItens });
  });

  protected statusPedidoLabel(): string {
    switch (this.mesa().statusPedido) {
      case 'EM_PREPARO':
        return this.translate.instant('TABLE.IN_PREPARATION');
      case 'PRONTO_ENTREGA':
        return this.translate.instant('TABLE.READY_FOR_DELIVERY');
      case 'CONTA_ABERTA':
        return this.translate.instant('TABLE.OPEN_BILL');
      default:
        return '';
    }
  }

  protected nomeEtapa(etapa: number | null): string {
    if (etapa === null) return '';
    const key = CHAVES_ETAPAS[etapa];
    return key ? this.translate.instant(key) : '';
  }

  protected tempoAtrasLabel(minutos: number): string {
    if (minutos < 60) return `${minutos} min`;

    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `${horas}h`;

    const dias = Math.floor(horas / 24);
    return `${dias}d`;
  }
}
