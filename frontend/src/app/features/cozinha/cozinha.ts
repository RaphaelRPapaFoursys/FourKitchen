import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  ItemFilaCozinhaResponse,
  PedidoFilaCozinhaResponse,
  SinalizarProblemaRequest,
} from '../../core/models/cozinha.models';
import { CozinhaService } from '../../core/services/cozinha';

type PrioridadePedido = 'urgente' | 'alta' | 'normal';
type OrdenacaoPedido = 'tempo' | 'prioridade';
type TipoProblema = SinalizarProblemaRequest['statusProdutoPedido'];

interface ItemPedidoSelecionado {
  pedido: PedidoFilaCozinhaResponse;
  item: ItemFilaCozinhaResponse;
}

@Component({
  selector: 'app-cozinha',
  imports: [FormsModule, NgTemplateOutlet],
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
  protected readonly sucesso = signal<string | null>(null);
  protected readonly pedidoEmAcao = signal<number | null>(null);
  protected readonly itemEmAcao = signal<number | null>(null);
  protected readonly problemaSelecionado = signal<ItemPedidoSelecionado | null>(null);
  protected readonly tipoProblema = signal<TipoProblema>('FALTA_PRODUTO');

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
          ...this.itensPedido(pedido).flatMap(item => [this.nomeItem(item), item.observacao]),
        ].join(' ').toLowerCase();

        return texto.includes(termo);
      })
      .sort((a, b) => {
        if (this.ordenacao() === 'prioridade') {
          return prioridadePeso[this.prioridade(a)] - prioridadePeso[this.prioridade(b)]
            || this.minutosDesdeReferencia(b) - this.minutosDesdeReferencia(a);
        }

        return this.minutosDesdeReferencia(b) - this.minutosDesdeReferencia(a);
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

  protected readonly aguardandoDecisao = computed(() =>
    this.pedidosFiltrados().filter(pedido => this.statusNormalizado(pedido) === 'AGUARDANDO_DECISAO')
  );

  protected readonly urgentes = computed(() =>
    this.pedidos().filter(pedido => this.prioridade(pedido) === 'urgente').length
  );

  protected readonly altas = computed(() =>
    this.pedidos().filter(pedido => this.prioridade(pedido) === 'alta').length
  );

  protected readonly tempoMedio = computed(() => {
    const pedidos = this.pedidos();
    const total = pedidos.reduce((soma, pedido) => soma + this.minutosDesdeReferencia(pedido), 0);

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

  protected limparMensagens(): void {
    this.erro.set(null);
    this.sucesso.set(null);
  }

  protected iniciarPreparo(id: number): void {
    this.alterarStatus(id, 'EM_PREPARO');
  }

  protected marcarPronto(id: number): void {
    this.alterarStatus(id, 'PRONTO');
  }

  protected abrirProblema(pedido: PedidoFilaCozinhaResponse, item: ItemFilaCozinhaResponse): void {
    this.limparMensagens();
    this.tipoProblema.set('FALTA_PRODUTO');
    this.problemaSelecionado.set({ pedido, item });
  }

  protected fecharProblema(): void {
    if (this.itemEmAcao() !== null) {
      return;
    }

    this.problemaSelecionado.set(null);
  }

  protected confirmarProblema(): void {
    const selecao = this.problemaSelecionado();

    if (!selecao) {
      return;
    }

    this.itemEmAcao.set(selecao.item.id);
    this.limparMensagens();

    this.cozinhaService.sinalizarProblema({
      idPedido: selecao.pedido.id,
      idProdutoPedido: selecao.item.id,
      statusProdutoPedido: this.tipoProblema(),
    }).subscribe({
      next: () => {
        this.sucesso.set('Problema sinalizado. Aguardando decisao do garcom.');
        this.problemaSelecionado.set(null);
        this.itemEmAcao.set(null);
        this.carregarFila();
      },
      error: erro => {
        this.erro.set(this.extrairMensagemErro(erro, 'Nao foi possivel sinalizar o problema.'));
        this.itemEmAcao.set(null);
      },
    });
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
    return this.minutosDesdeData(pedido.dataCriacao);
  }

  protected minutosEmPreparo(pedido: PedidoFilaCozinhaResponse): number {
    return this.minutosDesdeData(pedido.dataInicioPreparo ?? pedido.dataCriacao);
  }

  protected minutosDesdeReferencia(pedido: PedidoFilaCozinhaResponse): number {
    return this.statusNormalizado(pedido) === 'EM_PREPARO'
      ? this.minutosEmPreparo(pedido)
      : this.minutosDesdeCriacao(pedido);
  }

  protected statusPronto(pedido: PedidoFilaCozinhaResponse): boolean {
    return this.statusNormalizado(pedido) === 'PRONTO';
  }

  protected nomeItem(item: PedidoFilaCozinhaResponse['itens'][number]): string {
    return item.nomeProduto?.trim() || `Produto #${item.idProduto}`;
  }

  protected itensPedido(pedido: PedidoFilaCozinhaResponse): ItemFilaCozinhaResponse[] {
    return pedido.itens ?? [];
  }

  protected statusLabel(pedido: PedidoFilaCozinhaResponse): string {
    const labels: Record<string, string> = {
      ENVIADO_COZINHA: 'Aguardando',
      EM_PREPARO: 'Em preparo',
      AGUARDANDO_DECISAO: 'Aguardando decisao',
      PRONTO: 'Pronto',
      ENTREGUE: 'Entregue',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
    };

    return labels[this.statusNormalizado(pedido)] ?? pedido.status;
  }

  protected problemaLabel(tipo: TipoProblema): string {
    const labels: Record<TipoProblema, string> = {
      FALTA_PRODUTO: 'Falta de produto',
      ERRO: 'Erro no item',
      INDISPONIVEL: 'Produto indisponivel',
    };

    return labels[tipo];
  }

  protected acaoDesabilitada(pedido: PedidoFilaCozinhaResponse): boolean {
    return this.pedidoEmAcao() === pedido.id || this.statusPronto(pedido);
  }

  protected itemAcaoDesabilitada(item: ItemFilaCozinhaResponse): boolean {
    return this.itemEmAcao() === item.id;
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
      error: erro => {
        this.erro.set(this.extrairMensagemErro(erro, 'Nao foi possivel carregar a fila da cozinha.'));
        this.carregando.set(false);
      },
    });
  }

  private alterarStatus(id: number, status: 'EM_PREPARO' | 'PRONTO'): void {
    this.pedidoEmAcao.set(id);
    this.erro.set(null);

    this.cozinhaService.alterarStatus(id, status).subscribe({
      next: () => {
        this.sucesso.set(status === 'EM_PREPARO' ? 'Preparo iniciado.' : 'Pedido marcado como pronto.');
        this.pedidoEmAcao.set(null);
        this.carregarFila();
      },
      error: erro => {
        this.erro.set(this.extrairMensagemErro(erro, 'Nao foi possivel atualizar o pedido.'));
        this.pedidoEmAcao.set(null);
      },
    });
  }

  private statusNormalizado(pedido: PedidoFilaCozinhaResponse): string {
    return String(pedido.status).trim().toUpperCase();
  }

  private minutosDesdeData(dataIso: string | null | undefined): number {
    if (!dataIso) {
      return 0;
    }

    const data = new Date(dataIso).getTime();

    if (Number.isNaN(data)) {
      return 0;
    }

    return Math.max(0, Math.round((Date.now() - data) / 60000));
  }

  private extrairMensagemErro(erro: unknown, mensagemPadrao: string): string {
    if (erro instanceof HttpErrorResponse) {
      const corpo = erro.error as { msgError?: string } | null;

      if (corpo?.msgError) {
        return corpo.msgError;
      }
    }

    return mensagemPadrao;
  }
}
