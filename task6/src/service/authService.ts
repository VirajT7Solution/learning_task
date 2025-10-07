import axios, { type AxiosResponse } from 'axios';
import type { AuthResponse, LoginRequest, SignupRequest, RefreshTokenResponse } from '../types/types';

const API_BASE_URL = 'http://192.168.10.139:8080/api';

// Create axios instance for API calls
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Flag to prevent multiple refresh requests
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

// Timer for automatic token refresh
let refreshTimer: number | null = null;
let isRefreshingTokens = false; // Flag to prevent recursive timer setting
let lastRefreshTime = 0; // Track last refresh time to prevent rapid successive calls
let redirectingToLogin = false; // Prevent repeated redirects

const redirectToLogin = () => {
  if (redirectingToLogin) return;
  redirectingToLogin = true;
  try {
    window.location.assign('/login');
  } catch (_) {
    // no-op
  }
};

// Timer management functions
const TimerManager = {
  // Clear existing refresh timer
  clearRefreshTimer: () => {
    if (refreshTimer) {
      console.log('🛑 [TIMER] Clearing existing refresh timer');
      clearTimeout(refreshTimer);
      refreshTimer = null;
    } else {
      console.log('ℹ️ [TIMER] No existing timer to clear');
    }
  },

  // Set automatic refresh timer based on expiresInSeconds
  setRefreshTimer: (expiresInSeconds: string) => {
    TimerManager.clearRefreshTimer();
    
    const expiresInMs = parseInt(expiresInSeconds) * 1000;
    
    // Calculate refresh time based on token lifetime (TESTING MODE - using seconds)
    let refreshTime;
    if (expiresInMs <= 60000) { // If token expires in 1 minute or less
      // For short-lived tokens, refresh at 80% of the token lifetime
      refreshTime = Math.max(expiresInMs * 0.8, 5000); // At least 5 seconds
    } else {
      // For longer-lived tokens, refresh 10 seconds before expiry
      refreshTime = Math.max(expiresInMs - 10000, 10000); // At least 10 seconds
    }
    
    console.log(`🕐 [AUTO-REFRESH] Setting timer for ${refreshTime / 1000} seconds (token expires in ${expiresInMs / 1000} seconds)`);
    
    refreshTimer = setTimeout(async () => {
      try {
        console.log('🚀 [AUTO-REFRESH] Timer triggered - starting automatic refresh...');
        console.log('🔍 [AUTO-REFRESH] Current isRefreshingTokens flag:', isRefreshingTokens);
        console.log('🔍 [AUTO-REFRESH] Current lastRefreshTime:', lastRefreshTime);
        console.log('🔍 [AUTO-REFRESH] Time since last refresh:', Date.now() - lastRefreshTime);
        
        console.log('🔄 [AUTO-REFRESH] Calling refreshToken() function...');
        const result = await refreshToken();
        console.log('🔍 [AUTO-REFRESH] refreshToken() returned:', result);
        
        if (result) {
          console.log('✅ [AUTO-REFRESH] Automatic refresh completed successfully');
          
          // Get new expiry time and set next timer
          const newExpiresIn = localStorage.getItem('expiresInSeconds');
          if (newExpiresIn) {
            console.log('🔄 [AUTO-REFRESH] Setting new timer with refreshed token');
            TimerManager.setRefreshTimer(newExpiresIn);
          } else {
            console.log('⚠️ [AUTO-REFRESH] No new expiry time found after refresh');
          }
        } else {
          console.log('⚠️ [AUTO-REFRESH] refreshToken() returned null - no refresh occurred');
        }
      } catch (error) {
        console.error('❌ [AUTO-REFRESH] Automatic refresh failed:', error);
        TimerManager.clearRefreshTimer();
        // Clear tokens and redirect to login
        TokenUtils.clearTokens();
        window.location.href = '/login';
      } finally {
        // Don't reset isRefreshingTokens here - let refreshToken() handle it
        console.log('🔧 [AUTO-REFRESH] Timer callback completed');
      }
    }, refreshTime);
    
    console.log(`✅ [TIMER] Timer set successfully with ID: ${refreshTimer}`);
  },

  // Initialize timer on app start
  initializeTimer: () => {
    console.log('🔧 [AUTO-REFRESH] Initializing timer on app start...');
    const expiresIn = localStorage.getItem('expiresInSeconds');
    const issuedAt = localStorage.getItem('tokenIssuedAt');
    
    if (expiresIn && issuedAt) {
      const expiryTime = parseInt(issuedAt) + parseInt(expiresIn) * 1000;
      const timeUntilExpiry = expiryTime - Date.now();
      
      console.log(`🔍 [AUTO-REFRESH] Token expires in ${timeUntilExpiry / 1000} seconds`);
      
      // Only set timer if token hasn't expired yet (TESTING MODE - using seconds)
      if (timeUntilExpiry > 5000) { // At least 5 seconds remaining
        console.log('✅ [AUTO-REFRESH] Token is valid, setting refresh timer');
        TimerManager.setRefreshTimer(expiresIn);
      } else {
        console.log('❌ [AUTO-REFRESH] Token expired or expires too soon, clearing tokens');
        // Token is expired or expires very soon, clear it
        TokenUtils.clearTokens();
      }
    } else {
      console.log('⚠️ [AUTO-REFRESH] No tokens found in localStorage');
    }
  }
};

// Process failed requests queue
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  
  failedQueue = [];
};

// Token utility functions
const TokenUtils = {
  getAccessToken: (): string | null => {
    return localStorage.getItem('accessToken');
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem('refreshToken');
  },

  getTokenExpiry: (): number | null => {
    const expiresIn = localStorage.getItem('expiresInSeconds');
    if (!expiresIn) return null;
    
    const issuedAt = localStorage.getItem('tokenIssuedAt');
    if (!issuedAt) return null;
    
    return parseInt(issuedAt) + parseInt(expiresIn) * 1000;
  },

  setTokens: (accessToken: string, refreshToken: string, expiresInSeconds: string) => {
    console.log(`💾 [SET-TOKENS] Storing new tokens (expires in ${expiresInSeconds} seconds)`);
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('expiresInSeconds', expiresInSeconds);
    localStorage.setItem('tokenIssuedAt', Date.now().toString());
    
    // Set automatic refresh timer ONLY if not currently refreshing tokens
    if (!isRefreshingTokens) {
      console.log('⏰ [SET-TOKENS] Setting automatic refresh timer');
      TimerManager.setRefreshTimer(expiresInSeconds);
    } else {
      console.log('⏸️ [SET-TOKENS] Skipping timer setting (currently refreshing) - timer will be set after refresh completes');
    }
  },

  clearTokens: () => {
    console.log('🗑️ [CLEAR-TOKENS] Clearing all tokens and timers');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('expiresInSeconds');
    localStorage.removeItem('tokenIssuedAt');
    localStorage.removeItem('user');
    
    // Clear refresh timer
    TimerManager.clearRefreshTimer();
  },

  isTokenExpired: (): boolean => {
    const expiry = TokenUtils.getTokenExpiry();
    
    if (!expiry) return true;
    
    // Add 5 second buffer before actual expiry (TESTING MODE)
    const bufferTime = 5 * 1000; // 5 seconds in milliseconds
    return Date.now() >= (expiry - bufferTime);
  },

  isTokenExpiredSoon: (): boolean => {
    const expiry = TokenUtils.getTokenExpiry();
    if (!expiry) return true;
    
    // Check if token expires in next 10 seconds (TESTING MODE)
    const bufferTime = 10 * 1000; // 10 seconds in milliseconds
    return Date.now() >= (expiry - bufferTime);
  }
};

// Refresh token function
// force=true skips cooldown checks (used by interceptor only when necessary)
const refreshToken = async (force: boolean = false): Promise<string | null> => {
  const now = Date.now();
  
  console.log(`🔄 [REFRESH-TOKEN] Refresh token called (force: ${force})`);
  
  // Prevent multiple concurrent refresh attempts
  if (isRefreshingTokens) {
    console.log('⏸️ [REFRESH-TOKEN] Already refreshing, skipping...');
    return null;
  }

  // Prevent rapid successive refresh calls (minimum 5 seconds between refreshes for testing)
  if (!force && (now - lastRefreshTime < 5000)) {
    console.log(`⏸️ [REFRESH-TOKEN] Called too soon, skipping... (${(now - lastRefreshTime) / 1000}s since last refresh)`);
    return null;
  }

  const refreshTokenValue = TokenUtils.getRefreshToken();
  
  if (!refreshTokenValue) {
    console.log('❌ [REFRESH-TOKEN] No refresh token found, clearing tokens');
    TokenUtils.clearTokens();
    return null;
  }

  try {
    console.log('🚀 [REFRESH-TOKEN] Starting refresh token API call...');
    isRefreshingTokens = true;
    lastRefreshTime = now;
    
    const response: AxiosResponse<RefreshTokenResponse> = await axios.post(
      `${API_BASE_URL}/auth/refresh`,
      { refreshToken: refreshTokenValue }
    );

    const { accessToken, refreshToken: newRefreshToken, expiresInSeconds } = response.data;
    
    console.log(`✅ [REFRESH-TOKEN] API call successful, new token expires in ${expiresInSeconds} seconds`);
    TokenUtils.setTokens(accessToken, newRefreshToken, expiresInSeconds);
    
    return accessToken;
  } catch (error) {
    console.error('❌ [REFRESH-TOKEN] API call failed:', error);
    TokenUtils.clearTokens();
    // Don't throw here to avoid unhandled rejections causing crashes in some flows
    return null;
  } finally {
    isRefreshingTokens = false;
  }
};

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = TokenUtils.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // If the failing request is an auth endpoint itself, don't try to refresh
    const failingUrl: string = (originalRequest?.url || '').toString();
    const isAuthEndpoint = failingUrl.includes('/auth/login') || failingUrl.includes('/auth/register') || failingUrl.includes('/auth/refresh');
    if (isAuthEndpoint) {
      TokenUtils.clearTokens();
      redirectToLogin();
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // If already refreshing, queue the request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        }).catch((err) => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const newToken = await refreshToken(true); // force refresh when via interceptor
        processQueue(null, newToken);
        
        // Retry the original request with new token (only if we have one)
        if (newToken) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        } else {
          // No token available, redirect to login
          redirectToLogin();
          return Promise.reject(error);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        TokenUtils.clearTokens();
        // Redirect to login page or handle logout
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export const authService = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, data);
    const { accessToken, refreshToken: refreshTokenValue, expiresInSeconds, user } = response.data;
    
    TokenUtils.setTokens(accessToken, refreshTokenValue, expiresInSeconds);
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    }
    
    return response.data;
  },

  register: async (data: SignupRequest): Promise<any> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  refreshToken,

  logout: () => {
    TokenUtils.clearTokens();
  },

  getAccessToken: TokenUtils.getAccessToken,
  getRefreshToken: TokenUtils.getRefreshToken,
  isAuthenticated: (): boolean => {
    const token = TokenUtils.getAccessToken();
    const refreshTokenValue = TokenUtils.getRefreshToken();
    return !!(token && refreshTokenValue && !TokenUtils.isTokenExpired());
  },

  isTokenExpired: TokenUtils.isTokenExpired,
  isTokenExpiredSoon: TokenUtils.isTokenExpiredSoon,

  // Auto-refresh token if needed
  ensureValidToken: async (): Promise<boolean> => {
    if (TokenUtils.isTokenExpiredSoon()) {
      try {
        const token = await refreshToken();
        return !!token;
      } catch (error) {
        return false;
      }
    }
    return true;
  },

  // Get user info
  getUser: (): any => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  // Make authenticated API calls
  apiClient,

  // Timer management
  initializeTimer: TimerManager.initializeTimer,
  clearRefreshTimer: TimerManager.clearRefreshTimer
};