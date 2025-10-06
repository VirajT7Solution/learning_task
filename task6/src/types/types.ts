export interface User {
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  user?: User;
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