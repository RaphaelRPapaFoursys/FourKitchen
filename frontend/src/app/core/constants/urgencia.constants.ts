import { AcaoMesaPainel, MesaPainel } from '../models/painel.models';

export const LIMIARES_URGENCIA = {
  preparoAtencaoMinutos: 14,
  prontoOkMaxMinutos: 5,
  prontoAtencaoMaxMinutos: 10,
} as const;

export const LIMIARES_CARGA_GARCOM = {
  baixaMaxMesas: 1,
  mediaMaxMesas: 2,
} as const;

export type NivelCarga = 'BAIXA' | 'MEDIA' | 'ALTA';

export function nivelCargaGarcom(mesasAtivas: number): NivelCarga {
  if (mesasAtivas <= LIMIARES_CARGA_GARCOM.baixaMaxMesas) return 'BAIXA';
  if (mesasAtivas <= LIMIARES_CARGA_GARCOM.mediaMaxMesas) return 'MEDIA';
  return 'ALTA';
}

/**
 * critico/atencao: alerta de tempo (vermelho/laranja).
 * ok: sinal positivo explícito (ex.: pedido pronto dentro da meta).
 * emAndamento: fluxo normal, sem julgamento (ex.: em preparo dentro do tempo).
 * info: estado informativo que não é sobre urgência (ex.: conta aberta).
 * livre: status estrutural da mesa, não do pedido.
 */
export type Criticidade = 'critico' | 'atencao' | 'ok' | 'info' | 'emAndamento' | 'livre';

/** Deriva a criticidade só a partir do estado da mesa — nunca de um campo armazenado. */
export function resolverCriticidadeMesa(mesa: MesaPainel): Criticidade {
  if (mesa.status === 'LIVRE') return 'livre';

  if (mesa.statusPedido === 'EM_PREPARO') {
    return (mesa.tempoMinutos ?? 0) >= LIMIARES_URGENCIA.preparoAtencaoMinutos ? 'atencao' : 'emAndamento';
  }

  if (mesa.statusPedido === 'PRONTO_ENTREGA') {
    const minutos = mesa.tempoMinutos ?? 0;
    if (minutos > LIMIARES_URGENCIA.prontoAtencaoMaxMinutos) return 'critico';
    if (minutos > LIMIARES_URGENCIA.prontoOkMaxMinutos) return 'atencao';
    return 'ok';
  }

  if (mesa.statusPedido === 'CONTA_ABERTA') return 'info';
  return 'emAndamento';
}

/** Ação primária sugerida pelo card — derivada de status/statusPedido, nunca armazenada na mesa. */
export function resolverAcaoPrimaria(mesa: MesaPainel): { tipo: AcaoMesaPainel; label: string } {
  if (mesa.status === 'LIVRE') return { tipo: 'ABRIR_MESA', label: 'Abrir mesa' };
  if (mesa.statusPedido === 'PRONTO_ENTREGA') return { tipo: 'MARCAR_ENTREGUE', label: 'Marcar entregue' };
  if (mesa.statusPedido === 'CONTA_ABERTA') return { tipo: 'FECHAR_CONTA', label: 'Fechar conta' };
  return { tipo: 'VER_PEDIDO', label: 'Ver pedido' };
}
