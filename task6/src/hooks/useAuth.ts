import { useEffect, useState } from 'react';
import { authService } from '../service/authService';

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(authService.isAuthenticated());
  const [user, setUser] = useState(authService.getUser());
  const [isLoading, setIsLoading] = useState(false);

  // Check token validity and refresh if needed on component mount
  useEffect(() => {
    const checkAuthStatus = async () => {
      if (authService.isAuthenticated()) {
        setIsLoading(true);
        try {
          const isValid = await authService.ensureValidToken();
          if (!isValid) {
            setIsAuthenticated(false);
            setUser(null);
          } else {
            setIsAuthenticated(true);
            setUser(authService.getUser());
            // Initialize automatic refresh timer
            authService.initializeTimer();
          }
        } catch (error) {
          setIsAuthenticated(false);
          setUser(null);
        } finally {
          setIsLoading(false);
        }
      }
    };

    checkAuthStatus();
  }, []);

  const login = async (credentials: { username: string; password: string }) => {
    setIsLoading(true);
    try {
      await authService.login(credentials);
      setIsAuthenticated(true);
      setUser(authService.getUser());
      // Timer is automatically set in authService.login via setTokens
      return true;
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUser(null);
  };

  const refreshToken = async () => {
    setIsLoading(true);
    try {
      await authService.refreshToken();
      setUser(authService.getUser());
      return true;
    } catch (error) {
      setIsAuthenticated(false);
      setUser(null);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  // Cleanup timer on component unmount
  useEffect(() => {
    return () => {
      // Cleanup is handled by authService.logout() and clearTokens()
      // But we can also clear it here as a safety measure
      if (!authService.isAuthenticated()) {
        authService.clearRefreshTimer();
      }
    };
  }, []);

  return {
    isAuthenticated,
    user,
    isLoading,
    login,
    logout,
    refreshToken,
    ensureValidToken: authService.ensureValidToken,
    isTokenExpired: authService.isTokenExpired,
    isTokenExpiredSoon: authService.isTokenExpiredSoon
  };
};
