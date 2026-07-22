export interface TotemGestorResponse {
  id: number;
  nome: string;
  email: string;
  ativo: boolean;
  pedidosHoje: number;
  valorHoje: number;
  ultimaAtividade: string | null;
  problemasAbertos: number;
}
