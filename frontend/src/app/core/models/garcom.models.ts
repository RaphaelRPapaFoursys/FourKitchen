export interface PedidoAtivoMesaResponse {
  id: number;
  codigo: number;
  canal: string;
  status: string;
  idAtendimento: number | null;
}

export interface ChamadaPendenteMesaResponse {
  id: number;
  tipo: string;
  mensagem: string;
  data: string;
}

export interface MesaGarcomResponse {
  idMesa: number;
  numero: number;
  status: 'DISPONIVEL' | 'OCUPADA' | string;
  idAtendimento: number | null;
  codigoSessao: number | null;
  idGarcom: number | null;
  dataAbertura: string | null;
  pedidosAtivos: PedidoAtivoMesaResponse[];
  chamadasPendentes: ChamadaPendenteMesaResponse[];
  possuiChamadaPendente: boolean;
}
