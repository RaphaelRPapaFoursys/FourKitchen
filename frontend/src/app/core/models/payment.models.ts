export type PaymentMethod = 'CREDITO' | 'DEBITO' | 'PIX';

export interface PagamentoResponse {
  status: string;
  mensagem: string;
  codigoAutorizacao: string;
}
