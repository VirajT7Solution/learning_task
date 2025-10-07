import React, { useState, useEffect } from 'react';
import { authService } from '../service/authService';

const TokenTestHelper: React.FC = () => {
  const [timeLeft, setTimeLeft] = useState<number>(0);
  const [isExpired, setIsExpired] = useState<boolean>(false);
  const [isExpiredSoon, setIsExpiredSoon] = useState<boolean>(false);

  useEffect(() => {
    const updateStatus = () => {
      const expiresIn = localStorage.getItem('expiresInSeconds');
      const issuedAt = localStorage.getItem('tokenIssuedAt');
      
      if (expiresIn && issuedAt) {
        const expiryTime = parseInt(issuedAt) + parseInt(expiresIn) * 1000;
        const timeUntilExpiry = Math.max(expiryTime - Date.now(), 0);
        const secondsLeft = Math.floor(timeUntilExpiry / 1000);
        
        setTimeLeft(secondsLeft);
        setIsExpired(authService.isTokenExpired());
        setIsExpiredSoon(authService.isTokenExpiredSoon());
      } else {
        setTimeLeft(0);
        setIsExpired(true);
        setIsExpiredSoon(true);
      }
    };

    updateStatus();
    const interval = setInterval(updateStatus, 1000);
    
    return () => clearInterval(interval);
  }, []);

  const setTestToken = (seconds: number) => {
    const now = Date.now();
    localStorage.setItem('accessToken', 'test-access-token');
    localStorage.setItem('refreshToken', 'test-refresh-token');
    localStorage.setItem('expiresInSeconds', seconds.toString());
    localStorage.setItem('tokenIssuedAt', now.toString());
    localStorage.setItem('user', JSON.stringify({ username: 'testuser', email: 'test@test.com' }));
    
    // Initialize timer
    authService.initializeTimer();
    
    // Refresh the status
    window.location.reload();
  };

  const clearTokens = () => {
    authService.logout();
    window.location.reload();
  };

  return (
    <div style={{
      position: 'fixed',
      top: '10px',
      right: '10px',
      background: 'rgba(0,0,0,0.9)',
      color: 'white',
      padding: '15px',
      borderRadius: '8px',
      fontSize: '12px',
      maxWidth: '250px',
      zIndex: 9999
    }}>
      <h4 style={{ margin: '0 0 10px 0', color: '#4ade80' }}>🧪 Token Test Helper</h4>
      
      <div style={{ marginBottom: '8px' }}>
        <strong>Time Left:</strong> {timeLeft}s
      </div>
      
      <div style={{ marginBottom: '8px' }}>
        <strong>Expired:</strong> 
        <span style={{ color: isExpired ? '#ef4444' : '#10b981', marginLeft: '5px' }}>
          {isExpired ? 'YES' : 'NO'}
        </span>
      </div>
      
      <div style={{ marginBottom: '8px' }}>
        <strong>Expires Soon:</strong> 
        <span style={{ color: isExpiredSoon ? '#f59e0b' : '#10b981', marginLeft: '5px' }}>
          {isExpiredSoon ? 'YES' : 'NO'}
        </span>
      </div>

      <div style={{ marginTop: '10px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
        <button 
          onClick={() => setTestToken(30)}
          style={{
            padding: '5px 8px',
            fontSize: '10px',
            backgroundColor: '#ef4444',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Test 30s
        </button>
        
        <button 
          onClick={() => setTestToken(60)}
          style={{
            padding: '5px 8px',
            fontSize: '10px',
            backgroundColor: '#f59e0b',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Test 60s
        </button>
        
        <button 
          onClick={() => setTestToken(120)}
          style={{
            padding: '5px 8px',
            fontSize: '10px',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Test 2min
        </button>
        
        <button 
          onClick={clearTokens}
          style={{
            padding: '5px 8px',
            fontSize: '10px',
            backgroundColor: '#6b7280',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Clear
        </button>
      </div>
      
      <div style={{ 
        marginTop: '10px', 
        padding: '8px', 
        backgroundColor: '#1f2937', 
        borderRadius: '4px',
        fontSize: '10px'
      }}>
        <strong>Testing Mode:</strong>
        <br />• Refresh happens 10s before expiry
        <br />• 5s cooldown between refreshes
        <br />• Watch console for refresh logs
      </div>
    </div>
  );
};

export default TokenTestHelper;
