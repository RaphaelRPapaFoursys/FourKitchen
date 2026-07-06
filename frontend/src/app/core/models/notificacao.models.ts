export type DestinoNotificacao = 'GARCOM' | 'MESA' | 'TOTEM' | 'COZINHA' | 'GESTOR';

export interface NotificacaoResponse {
  id: number;
  tipo: string;
  mensagem: string;
  destino: DestinoNotificacao;
  lida: boolean;
  data: string;
}
