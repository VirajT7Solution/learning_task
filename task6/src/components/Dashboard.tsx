import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../service/authService';
import TokenTestHelper from './TokenTestHelper';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [tokenInfo, setTokenInfo] = useState<any>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const updateTokenInfo = useCallback(() => {
    const user = authService.getUser();
    const isExpired = authService.isTokenExpired();
    const isExpiredSoon = authService.isTokenExpiredSoon();
    const expiresIn = localStorage.getItem('expiresInSeconds');
    const issuedAt = localStorage.getItem('tokenIssuedAt');
    
    let timeRemaining = '';
    if (expiresIn && issuedAt) {
      const expiryTime = parseInt(issuedAt) + parseInt(expiresIn) * 1000;
      const timeUntilExpiry = expiryTime - Date.now();
      
      if (timeUntilExpiry > 0) {
        const totalSeconds = Math.floor(timeUntilExpiry / 1000);
        timeRemaining = `${totalSeconds}s`;
      } else {
        timeRemaining = 'Expired';
      }
    }

    setTokenInfo({
      user,
      isExpired,
      isExpiredSoon,
      expiresIn,
      timeRemaining
    });
  }, []);

  // Check authentication status and redirect if needed
  useEffect(() => {
    const checkAuth = () => {
      const authenticated = authService.isAuthenticated();
      const hasTokens = !!(localStorage.getItem('accessToken') && localStorage.getItem('refreshToken'));
      
      setIsAuthenticated(authenticated);
      
      // Only redirect if we have no tokens at all
      if (!hasTokens) {
        navigate('/login');
      }
    };

    checkAuth();
  }, [navigate]);

  useEffect(() => {
    if (!isAuthenticated) return;

    updateTokenInfo();
    
    // Update token info every second
    const interval = setInterval(updateTokenInfo, 1000);
    
    return () => clearInterval(interval);
  }, [isAuthenticated, updateTokenInfo]);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const handleManualRefresh = async () => {
    try {
      await authService.refreshToken();
      updateTokenInfo();
    } catch (error) {
      // Handle error silently or show user-friendly message
    }
  };

  // Don't render if no tokens at all
  const hasTokens = !!(localStorage.getItem('accessToken') && localStorage.getItem('refreshToken'));
  if (!hasTokens) {
    return null;
  }

  return (
    <div className="auth-container">
      <TokenTestHelper />
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-icon">
            <span style={{ fontSize: '24px' }}>🏠</span>
          </div>
          <h1 className="auth-title">
            Dashboard
          </h1>
          <p className="auth-subtitle">
            Welcome back, {tokenInfo?.user?.username || 'User'}!
          </p>
          <div style={{
            padding: '8px 16px',
            borderRadius: '8px',
            backgroundColor: isAuthenticated ? '#dcfce7' : '#fef3c7',
            border: `1px solid ${isAuthenticated ? '#16a34a' : '#d97706'}`,
            color: isAuthenticated ? '#166534' : '#92400e',
            fontSize: '14px',
            marginTop: '10px'
          }}>
            Status: {isAuthenticated ? '✅ Authenticated' : '⚠️ Authentication Issue'}
          </div>
        </div>


        <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
          <button 
            onClick={handleManualRefresh}
            style={{
              flex: 1,
              padding: '0.75rem',
              backgroundColor: '#667eea',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer'
            }}
          >
            Manual Refresh
          </button>
          
          <button 
            onClick={handleLogout}
            style={{
              flex: 1,
              padding: '0.75rem',
              backgroundColor: '#dc2626',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>

      </div>
    </div>
  );
};

export default Dashboard;
