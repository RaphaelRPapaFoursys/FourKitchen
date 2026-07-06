export type StatusMesa = 'DISPONIVEL' | 'OCUPADA';

export interface MesaResponse {
  id: number;
  numero: number;
  status: StatusMesa;
  garcomId: number | null;
  codigoSessao: number | null;
  dataAbertura: string | null;
  dataFechamento: string | null;
}

export interface CriarMesaRequest {
  numero: number;
}

export interface AtribuirGarcomRequest {
  garcomId: number;
}
