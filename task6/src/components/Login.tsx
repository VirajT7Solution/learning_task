import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, Lock, User } from 'lucide-react';
import { authService } from '../service/authService';
import type { LoginRequest } from '../types/types';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [loginData, setLoginData] = useState<LoginRequest>({
    username: '',
    password: ''
  });
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.login(loginData);
      
      // Wait a moment to ensure tokens are set
      setTimeout(() => {
        // Navigate to dashboard or home
        navigate('/dashboard');
      }, 100);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setLoginData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-icon">
            <Lock size={24} />
          </div>
          <h1 className="auth-title">
            Welcome Back
          </h1>
          <p className="auth-subtitle">
            Sign in to your account
          </p>
        </div>

        {error && (
          <div className="error-message">
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleLogin} className="auth-form">
          <div className="form-group">
            <label htmlFor="username" className="form-label">
              Username
            </label>
            <div className="input-wrapper">
              <User className="input-icon" size={20} />
              <input
                type="text"
                id="username"
                name="username"
                value={loginData.username}
                onChange={handleInputChange}
                placeholder="Enter username"
                required
                autoComplete="username"
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">
              Password
            </label>
            <div className="input-wrapper">
              <Lock className="input-icon" size={20} />
              <input
                type="password"
                id="password"
                name="password"
                value={loginData.password}
                onChange={handleInputChange}
                placeholder="Enter password"
                required
                autoComplete="current-password"
                className="form-input"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <div className="loading-spinner"></div>
                Signing in...
              </>
            ) : (
              'Sign In'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Don't have an account?{' '}
            <button className="auth-link" onClick={() => navigate('/signup')}>
              Sign up
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
