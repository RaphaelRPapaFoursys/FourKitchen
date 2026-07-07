import { MesaPainel, Pedido, ResumoExpediente, StatusPedidoPainel } from '../models/painel.models';

const MAXIMO_MESAS_MAIS_OCUPADAS = 5;

/** Snapshot de um atendimento no momento em que a conta foi fechada — preserva as métricas do expediente mesmo depois que a mesa é liberada. */
export interface AtendimentoFinalizado {
  numeroMesa: number;
  garcom: string;
  pedidos: Pedido[];
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

export function montarResumoExpediente(
  mesas: MesaPainel[],
  historico: AtendimentoFinalizado[],
): ResumoExpediente {
  const atendimentos: AtendimentoFinalizado[] = [
    ...historico,
    ...mesasComAtendimentoAtivo(mesas).map(mesa => ({
      numeroMesa: mesa.numero,
      garcom: mesa.garcom as string,
      pedidos: mesa.pedidos,
    })),
  ];
  const todosPedidos = atendimentos.flatMap(atendimento => atendimento.pedidos);

  const atendimentosPorGarcom = new Map<string, number>();
  for (const atendimento of atendimentos) {
    atendimentosPorGarcom.set(atendimento.garcom, (atendimentosPorGarcom.get(atendimento.garcom) ?? 0) + 1);
  }

  const garcomDestaque =
    [...atendimentosPorGarcom.entries()]
      .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
      .map(([nome, atendimentos]) => ({ nome, atendimentos }))[0] ?? null;

  const temposPreparo = todosPedidos
    .map(pedido => pedido.tempoPreparoMinutos)
    .filter((tempo): tempo is number => tempo !== null);
  const tempoMedioPreparoMin =
    temposPreparo.length > 0
      ? Math.round(temposPreparo.reduce((total, tempo) => total + tempo, 0) / temposPreparo.length)
      : null;

  const pedidosPorMesa = new Map<number, number>();
  for (const atendimento of atendimentos) {
    pedidosPorMesa.set(
      atendimento.numeroMesa,
      (pedidosPorMesa.get(atendimento.numeroMesa) ?? 0) + atendimento.pedidos.length,
    );
  }
  const mesasMaisOcupadas = [...pedidosPorMesa.entries()]
    .sort((a, b) => b[1] - a[1] || a[0] - b[0])
    .slice(0, MAXIMO_MESAS_MAIS_OCUPADAS)
    .map(([numeroMesa, pedidos]) => ({ numeroMesa, pedidos }));

  return {
    valorTotal: todosPedidos.reduce((total, pedido) => total + pedido.valor, 0),
    atendimentosRealizados: atendimentos.length,
    pedidosFeitos: todosPedidos.length,
    // TODO: regra de "problema" no resumo do expediente ainda não foi definida (não é a mesma
    // coisa que a criticidade por tempo usada no grid ao vivo — precisa de decisão à parte).
    problemas: 0,
    garcomDestaque,
    tempoMedioPreparoMin,
    mesasMaisOcupadas,
  };
}
