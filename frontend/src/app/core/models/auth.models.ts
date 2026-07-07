export interface LoginFormValue {
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioAutenticadoResponse {
  id: number;
  nome: string;
  email: string;
  perfil: string;
  idMesa?: number | null;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  usuario: UsuarioAutenticadoResponse;
}

export interface ApiError {
  codError: string;
  msgError: string;
}
