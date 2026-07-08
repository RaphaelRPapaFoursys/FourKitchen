import { MesaPainel, ResumoExpediente, StatusPedidoPainel } from '../models/painel.models';

const MAXIMO_MESAS_MAIS_OCUPADAS = 5;

/**
 * Registro de atendimento finalizado vindo do backend (`GET /api/gestor/atendimentos/historico`).
 * Só os campos usados no resumo do expediente; a API devolve mais atributos.
 */
export interface HistoricoAtendimentoExpediente {
  numeroMesa: number;
  nomeGarcom: string | null;
  valorFinal: number;
  totalPedidos: number;
}

/** Status do pedido em que a mesa ainda não recebeu tudo o que pediu. */
export const STATUS_PEDIDO_PENDENTES_ENTREGA: StatusPedidoPainel[] = ['EM_PREPARO', 'PRONTO_ENTREGA'];

export function mesasComAtendimentoAtivo(mesas: MesaPainel[]): MesaPainel[] {
  return mesas.filter(mesa => mesa.status === 'OCUPADA' && mesa.garcom !== null && mesa.pedidos.length > 0);
}

/** Quantidade de pedidos em mesas cujo status ainda não chegou à entrega. */
export function contarPedidosPendentesEntrega(mesas: MesaPainel[]): number {
  return mesas
    .filter(
      mesa =>
        mesa.status === 'OCUPADA' &&
        mesa.statusPedido !== null &&
        STATUS_PEDIDO_PENDENTES_ENTREGA.includes(mesa.statusPedido),
    )
    .reduce((total, mesa) => total + mesa.pedidos.length, 0);
}

/** Mesas ocupadas cuja conta ainda não foi fechada (receita não cobrada). */
export function contarMesasComContaAberta(mesas: MesaPainel[]): number {
  return mesas.filter(mesa => mesa.status === 'OCUPADA' && mesa.statusPedido === 'CONTA_ABERTA').length;
}

/**
 * Monta o resumo do expediente a partir dos atendimentos já finalizados no expediente atual
 * (histórico persistido no backend). Atendimentos ainda em andamento não entram: só contam
 * os que foram efetivamente fechados.
 */
export function montarResumoExpediente(
  atendimentos: HistoricoAtendimentoExpediente[],
): ResumoExpediente {
  const valorTotal = atendimentos.reduce((total, item) => total + item.valorFinal, 0);
  const pedidosFeitos = atendimentos.reduce((total, item) => total + item.totalPedidos, 0);

  const atendimentosPorGarcom = new Map<string, number>();
  for (const item of atendimentos) {
    const nome = item.nomeGarcom ?? '—';
    atendimentosPorGarcom.set(nome, (atendimentosPorGarcom.get(nome) ?? 0) + 1);
  }

  const garcomDestaque =
    [...atendimentosPorGarcom.entries()]
      .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
      .map(([nome, atendimentos]) => ({ nome, atendimentos }))[0] ?? null;

  const pedidosPorMesa = new Map<number, number>();
  for (const item of atendimentos) {
    pedidosPorMesa.set(item.numeroMesa, (pedidosPorMesa.get(item.numeroMesa) ?? 0) + item.totalPedidos);
  }
  const mesasMaisOcupadas = [...pedidosPorMesa.entries()]
    .sort((a, b) => b[1] - a[1] || a[0] - b[0])
    .slice(0, MAXIMO_MESAS_MAIS_OCUPADAS)
    .map(([numeroMesa, pedidos]) => ({ numeroMesa, pedidos }));

  return {
    valorTotal,
    atendimentosRealizados: atendimentos.length,
    pedidosFeitos,
    // TODO: regra de "problema" no resumo do expediente ainda não foi definida (não é a mesma
    // coisa que a criticidade por tempo usada no grid ao vivo — precisa de decisão à parte).
    problemas: 0,
    garcomDestaque,
    // TODO: backend ainda não registra o tempo de preparo por pedido no histórico.
    tempoMedioPreparoMin: null,
    mesasMaisOcupadas,
  };
}
