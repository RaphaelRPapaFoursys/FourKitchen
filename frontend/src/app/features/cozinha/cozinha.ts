import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';

import { PedidoFilaCozinhaResponse } from '../../core/models/cozinha.models';
import { CozinhaService } from '../../core/services/cozinha';

type PrioridadePedido = 'urgente' | 'alta' | 'normal';
type OrdenacaoPedido = 'tempo' | 'prioridade';

@Component({
  selector: 'app-cozinha',
  imports: [NgTemplateOutlet],
  templateUrl: './cozinha.html',
  styleUrl: './cozinha.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Cozinha {
  protected readonly pedidos = signal<PedidoFilaCozinhaResponse[]>([]);
  protected readonly busca = signal('');
  protected readonly ordenacao = signal<OrdenacaoPedido>('tempo');
  protected readonly sincronizadoEm = signal<Date | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly pedidoEmAcao = signal<number | null>(null);

  constructor(private readonly cozinhaService: CozinhaService) {
    this.carregarFila();
  }

  protected readonly pedidosFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const prioridadePeso: Record<PrioridadePedido, number> = {
      urgente: 0,
      alta: 1,
      normal: 2,
    };

    return this.pedidos()
      .filter(pedido => {
        if (!termo) {
          return true;
        }

        const texto = [
          pedido.id,
          pedido.codigo,
          pedido.status,
          this.origem(pedido),
          this.prioridade(pedido),
          ...pedido.itens.flatMap(item => [item.nomeProduto, item.observacao]),
        ].join(' ').toLowerCase();

        return texto.includes(termo);
      })
      .sort((a, b) => {
        if (this.ordenacao() === 'prioridade') {
          return prioridadePeso[this.prioridade(a)] - prioridadePeso[this.prioridade(b)]
            || this.minutosDesdeCriacao(b) - this.minutosDesdeCriacao(a);
        }

        return this.minutosDesdeCriacao(b) - this.minutosDesdeCriacao(a);
      });
  });

  protected readonly aguardando = computed(() =>
    this.pedidosFiltrados().filter(pedido => this.statusNormalizado(pedido) === 'ENVIADO_COZINHA')
  );

  protected readonly preparando = computed(() =>
    this.pedidosFiltrados().filter(pedido => {
      const status = this.statusNormalizado(pedido);

      return status === 'EM_PREPARO' || status === 'PRONTO';
    })
  );

  protected readonly urgentes = computed(() =>
    this.pedidos().filter(pedido => this.prioridade(pedido) === 'urgente').length
  );

  protected readonly altas = computed(() =>
    this.pedidos().filter(pedido => this.prioridade(pedido) === 'alta').length
  );

  protected readonly tempoMedio = computed(() => {
    const pedidos = this.pedidos();
    const total = pedidos.reduce((soma, pedido) => soma + this.minutosDesdeCriacao(pedido), 0);

    return pedidos.length ? Math.round(total / pedidos.length) : 0;
  });

  protected atualizarBusca(event: Event): void {
    this.busca.set((event.target as HTMLInputElement).value);
  }

  protected alternarOrdenacao(): void {
    this.ordenacao.update(valor => valor === 'tempo' ? 'prioridade' : 'tempo');
  }

  protected atualizarFila(): void {
    this.carregarFila();
  }

  protected iniciarPreparo(id: number): void {
    this.alterarStatus(id, 'EM_PREPARO');
  }

  protected marcarPronto(id: number): void {
    this.alterarStatus(id, 'PRONTO');
  }

  protected prioridadeLabel(prioridade: PrioridadePedido): string {
    const labels: Record<PrioridadePedido, string> = {
      urgente: 'Urgente',
      alta: 'Alta',
      normal: 'Normal',
    };

    return labels[prioridade];
  }

  protected prioridade(pedido: PedidoFilaCozinhaResponse): PrioridadePedido {
    const minutos = this.minutosDesdeCriacao(pedido);

    if (minutos >= 10) {
      return 'urgente';
    }

    if (minutos >= 5) {
      return 'alta';
    }

    return 'normal';
  }

  protected origem(pedido: PedidoFilaCozinhaResponse): string {
    if (pedido.idMesa) {
      return `Mesa ${pedido.idMesa.toString().padStart(2, '0')}`;
    }

    return pedido.canal.toLowerCase() === 'totem' ? 'Totem' : pedido.canal;
  }

  protected horario(pedido: PedidoFilaCozinhaResponse): string {
    return new Date(pedido.dataCriacao).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  protected minutosDesdeCriacao(pedido: PedidoFilaCozinhaResponse): number {
    const criadoEm = new Date(pedido.dataCriacao).getTime();

    if (Number.isNaN(criadoEm)) {
      return 0;
    }

    return Math.max(0, Math.round((Date.now() - criadoEm) / 60000));
  }

  protected statusPronto(pedido: PedidoFilaCozinhaResponse): boolean {
    return this.statusNormalizado(pedido) === 'PRONTO';
  }

  protected acaoDesabilitada(pedido: PedidoFilaCozinhaResponse): boolean {
    return this.pedidoEmAcao() === pedido.id || this.statusPronto(pedido);
  }

  protected sincronizadoLabel(): string {
    const sincronizadoEm = this.sincronizadoEm();

    if (!sincronizadoEm) {
      return 'pendente';
    }

    const segundos = Math.max(0, Math.round((Date.now() - sincronizadoEm.getTime()) / 1000));

    return segundos <= 1 ? 'agora' : `${segundos}s atras`;
  }

  private carregarFila(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.cozinhaService.listarFila().subscribe({
      next: pedidos => {
        this.pedidos.set(pedidos);
        this.sincronizadoEm.set(new Date());
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Nao foi possivel carregar a fila da cozinha.');
        this.carregando.set(false);
      },
    });
  }

  private alterarStatus(id: number, status: 'EM_PREPARO' | 'PRONTO'): void {
    this.pedidoEmAcao.set(id);
    this.erro.set(null);

    this.cozinhaService.alterarStatus(id, status).subscribe({
      next: () => {
        this.pedidoEmAcao.set(null);
        this.carregarFila();
      },
      error: () => {
        this.erro.set('Nao foi possivel atualizar o pedido.');
        this.pedidoEmAcao.set(null);
      },
    });
  }

  private statusNormalizado(pedido: PedidoFilaCozinhaResponse): string {
    return String(pedido.status).trim().toUpperCase();
  }
}
