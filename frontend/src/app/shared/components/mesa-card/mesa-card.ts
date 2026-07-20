import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { Criticidade } from '../../../core/constants/urgencia.constants';
import { AcaoMesaPainel, MesaPainel } from '../../../core/models/painel.models';
import { Badge } from '../badge/badge';
import { Icon } from '../icon/icon';
import { ProgressBar } from '../progress-bar/progress-bar';

const NOMES_ETAPAS: Record<number, string> = {
  1: 'Pedido enviado à cozinha',
  2: 'Em preparo',
  3: 'Finalização',
  4: 'Pronto para entrega',
};

/**
 * Card de mesa do painel do gestor. Apresentacional: recebe a mesa e as derivações
 * (criticidade, ação primária, bloqueio) prontas; emite `acao`/`trocar`/`verPedido`.
 * O :host é o próprio card e carrega o `id="mesa-card-N"` usado como âncora de destaque.
 */
@Component({
  selector: 'fk-mesa-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Badge, Icon, ProgressBar],
  host: {
    class: 'mesa-card',
    '[id]': "'mesa-card-' + mesa().numero",
    '[class.mesa-card--destacada]': 'destacada()',
  },
  templateUrl: './mesa-card.html',
  styleUrl: './mesa-card.scss',
})
export class MesaCard {
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
    if (criticidade === 'critico') return 'Atrasada';
    if (criticidade === 'atencao') return 'Atenção';
    return null;
  });

  protected readonly temAcaoSecundaria = computed(
    () =>
      this.mesa().status === 'OCUPADA' &&
      this.mesa().pedidos.length > 0 &&
      this.acaoPrimaria().tipo !== 'VER_PEDIDO',
  );

  protected readonly acaoPrimariaIndisponivel = computed(
    () => this.acaoPrimaria().tipo !== 'VER_PEDIDO' && this.bloqueado(),
  );

  protected statusPedidoLabel(): string {
    switch (this.mesa().statusPedido) {
      case 'EM_PREPARO':
        return 'Em preparo';
      case 'PRONTO_ENTREGA':
        return 'Pronto para entrega';
      case 'CONTA_ABERTA':
        return 'Conta aberta';
      default:
        return '';
    }
  }

  protected nomeEtapa(etapa: number | null): string {
    if (etapa === null) return '';
    return NOMES_ETAPAS[etapa] ?? '';
  }

  protected tempoAtrasLabel(minutos: number): string {
    if (minutos < 60) return `${minutos} min`;

    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `${horas}h`;

    const dias = Math.floor(horas / 24);
    return `${dias}d`;
  }
}
