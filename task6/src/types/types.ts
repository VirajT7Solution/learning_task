export interface User {
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: string;
  user?: User;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  role: string;
}