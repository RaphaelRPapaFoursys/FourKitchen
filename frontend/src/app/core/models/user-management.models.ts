export type PerfilUsuario = 'ADMIN' | 'GESTOR' | 'GARCOM' | 'COZINHA' | 'MESA' | 'TOTEM' | 'BALCAO';

export interface UsuarioGestorResponse {
  id: number;
  nome: string;
  email: string;
  perfilUsuario: PerfilUsuario;
  idMesa: number | null;
  ativo: boolean;
}

export interface CriarUsuarioGestorRequest {
  nome: string;
  email: string;
  senha: string;
  perfilUsuario: PerfilUsuario;
  idMesa: number | null;
}

export interface AtualizarUsuarioGestorRequest {
  nome: string;
  email: string;
  senha: string | null;
  perfilUsuario: PerfilUsuario;
  idMesa: number | null;
}
