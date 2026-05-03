export interface AuthLoginRequest {
  username: string;
  password: string;
}

export interface UsuarioInfo {
  id: string;
  nombre: string;
  rol: string;
}

export interface AuthLoginResponse {
  token: string;
  usuario: UsuarioInfo;
  expiresAt: string;
}
