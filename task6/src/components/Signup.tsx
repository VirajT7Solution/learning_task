import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, User, Mail, Lock } from 'lucide-react';

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  role: string;
}

const Signup = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [loading, setLoading] = useState(false);

  // Basic email regex pattern
  const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;

  // Basic password strength check (must be at least 8 characters)
  const isPasswordStrong = (password: string) => password.length >= 8;

  const validateForm = () => {
    const newErrors: { [key: string]: string } = {};

    if (!username) newErrors.username = 'Username is required';
    if (!email) {
      newErrors.email = 'Email is required';
    } else if (!emailPattern.test(email)) {
      newErrors.email = 'Please enter a valid email address';
    }
    if (!password) {
      newErrors.password = 'Password is required';
    } else if (!isPasswordStrong(password)) {
      newErrors.password = 'Password must be at least 8 characters long';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    setLoading(true);

    const signupData: SignupRequest = {
      username,
      email,
      password,
      role:"admin" // You can modify roles as needed based on your logic
    };

    try {
      // API call to register user
      const response = await fetch('http://192.168.10.139:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(signupData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Signup failed');
      }

      const data = await response.json();
      // Handle successful signup response (e.g., redirect to login)
      navigate('/login');
    } catch (err) {
      setErrors({
        global: err instanceof Error ? err.message : 'Something went wrong',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-icon">
            <User size={24} />
          </div>
          <h2 className="auth-title">
            Create Account
          </h2>
          <p className="auth-subtitle">
            Join us today and get started
          </p>
        </div>

        {errors.global && (
          <div className="error-message">
            <AlertCircle size={20} />
            <span>{errors.global}</span>
          </div>
        )}

        <form onSubmit={handleSignup} className="auth-form">
          <div className="form-group">
            <label htmlFor="username" className="form-label">
              Username
            </label>
            <div className="input-wrapper">
              <User className="input-icon" size={20} />
              <input
                type="text"
                id="username"
                placeholder="Enter your username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="form-input"
              />
            </div>
            {errors.username && <p className="field-error">{errors.username}</p>}
          </div>

          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Email Address
            </label>
            <div className="input-wrapper">
              <Mail className="input-icon" size={20} />
              <input
                type="email"
                id="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input"
              />
            </div>
            {errors.email && <p className="field-error">{errors.email}</p>}
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
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-input"
              />
            </div>
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <div className="loading-spinner"></div>
                Signing up...
              </>
            ) : (
              'Sign Up'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Already have an account?{' '}
            <button className="auth-link" onClick={() => navigate('/login')}>
              Log in
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;
