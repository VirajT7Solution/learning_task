import axios from 'axios';
import type { AuthResponse, LoginRequest, SignupRequest } from '../types/types';

const API_BASE_URL = 'http://localhost:8080/api';

export const authService = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, data);
    return response.data;
  },

  register: async (data: SignupRequest): Promise<any> => {
    const response = await axios.post(`${API_BASE_URL}/auth/register`, data);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  }
};