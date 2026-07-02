import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';

type StatusPedido = 'aguardando' | 'preparando';
type PrioridadePedido = 'urgente' | 'alta' | 'normal';
type OrdenacaoPedido = 'tempo' | 'prioridade';

interface ItemPedido {
  quantidade: number;
  nome: string;
  observacao?: string;
}

interface PedidoCozinha {
  id: number;
  status: StatusPedido;
  prioridade: PrioridadePedido;
  origem: string;
  horario: string;
  estimativaMinutos: number;
  noFogoMinutos: number;
  itens: ItemPedido[];
}

const pedidosIniciais: PedidoCozinha[] = [
  {
    id: 429,
    status: 'aguardando',
    prioridade: 'urgente',
    origem: 'Mesa 12',
    horario: '18:45',
    estimativaMinutos: 12,
    noFogoMinutos: 12,
    itens: [
      { quantidade: 2, nome: 'Hamburguer Gourmet Monster', observacao: 'Sem cebola, bem passado' },
      { quantidade: 1, nome: 'Batata Rustica Individual' },
    ],
  },
  {
    id: 432,
    status: 'aguardando',
    prioridade: 'alta',
    origem: 'Totem 02',
    horario: '18:52',
    estimativaMinutos: 5,
    noFogoMinutos: 5,
    itens: [
      { quantidade: 3, nome: 'Rodizio de Mini Burguer', observacao: '+ Molho extra barbecue' },
    ],
  },
  {
    id: 425,
    status: 'preparando',
    prioridade: 'alta',
    origem: 'Mesa Digital 08',
    horario: '08:12',
    estimativaMinutos: 8,
    noFogoMinutos: 8,
    itens: [
      { quantidade: 1, nome: 'Costela BBQ 12h', observacao: 'Ponto da carne: Desmanchando' },
      { quantidade: 1, nome: 'Arroz Biroska' },
    ],
  },
  {
    id: 435,
    status: 'preparando',
    prioridade: 'normal',
    origem: 'Mesa 04',
    horario: '02:45',
    estimativaMinutos: 2,
    noFogoMinutos: 2,
    itens: [
      { quantidade: 4, nome: 'Petit Gateau Classico' },
    ],
  },
];

@Component({
  selector: 'app-cozinha',
  imports: [NgTemplateOutlet],
  templateUrl: './cozinha.html',
  styleUrl: './cozinha.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Cozinha {
  protected readonly pedidos = signal<PedidoCozinha[]>(pedidosIniciais);
  protected readonly busca = signal('');
  protected readonly ordenacao = signal<OrdenacaoPedido>('tempo');
  protected readonly sincronizadoEm = signal(new Date(Date.now() - 2000));

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
          pedido.origem,
          pedido.prioridade,
          ...pedido.itens.flatMap(item => [item.nome, item.observacao]),
        ].join(' ').toLowerCase();

        return texto.includes(termo);
      })
      .sort((a, b) => {
        if (this.ordenacao() === 'prioridade') {
          return prioridadePeso[a.prioridade] - prioridadePeso[b.prioridade]
            || a.estimativaMinutos - b.estimativaMinutos;
        }

        return b.noFogoMinutos - a.noFogoMinutos;
      });
  });

  protected readonly aguardando = computed(() =>
    this.pedidosFiltrados().filter(pedido => pedido.status === 'aguardando')
  );

  protected readonly preparando = computed(() =>
    this.pedidosFiltrados().filter(pedido => pedido.status === 'preparando')
  );

  protected readonly urgentes = computed(() =>
    this.pedidos().filter(pedido => pedido.prioridade === 'urgente').length
  );

  protected readonly altas = computed(() =>
    this.pedidos().filter(pedido => pedido.prioridade === 'alta').length
  );

  protected readonly tempoMedio = computed(() => {
    const pedidos = this.pedidos();
    const total = pedidos.reduce((soma, pedido) => soma + pedido.noFogoMinutos, 0);

    return pedidos.length ? Math.round(total / pedidos.length) : 0;
  });

  protected atualizarBusca(event: Event): void {
    this.busca.set((event.target as HTMLInputElement).value);
  }

  protected alternarOrdenacao(): void {
    this.ordenacao.update(valor => valor === 'tempo' ? 'prioridade' : 'tempo');
  }

  protected atualizarFila(): void {
    this.sincronizadoEm.set(new Date());
  }

  protected iniciarPreparo(id: number): void {
    this.pedidos.update(pedidos =>
      pedidos.map(pedido =>
        pedido.id === id ? { ...pedido, status: 'preparando' } : pedido
      )
    );
  }

  protected marcarPronto(id: number): void {
    this.pedidos.update(pedidos => pedidos.filter(pedido => pedido.id !== id));
  }

  protected prioridadeLabel(prioridade: PrioridadePedido): string {
    const labels: Record<PrioridadePedido, string> = {
      urgente: 'Urgente',
      alta: 'Alta',
      normal: 'Normal',
    };

    return labels[prioridade];
  }

  protected sincronizadoLabel(): string {
    const segundos = Math.max(0, Math.round((Date.now() - this.sincronizadoEm().getTime()) / 1000));

    return segundos <= 1 ? 'agora' : `${segundos}s atras`;
  }
}
