import React, { useState } from 'react';
import api from './api';
import './SignIn.css'
import { AuthRequest, AuthResponse } from './types';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Shield, User, Lock } from 'lucide-react';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async () => {
    setMessage(null);
    try {
      const data: AuthRequest = { email, password };
      const response = await api.post<AuthResponse>('/auth/login', data);
      const tokenValue = response.data.tokenType
        ? `${response.data.tokenType} ${response.data.token}`
        : response.data.token;
      localStorage.setItem('token', tokenValue);
      navigate('/files');
    } catch (error: any) {
      if (error.response && error.response.data) {
        setMessage(error.response.data.message || 'Giriş başarısız.');
      } else {
        setMessage('Sunucu hatası.');
      }
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <Shield className="logo-icon" />
          </div>
          <h1>Secure Transfer</h1>
          <p className="login-subtitle">Güvenli dosya paylaşım platformu</p>
        </div>

        <div className="auth-tabs">
          <Link to="/login" className="auth-tab active">Giriş Yap</Link>
          <Link to="/register" className="auth-tab">Kayıt Ol</Link>
        </div>

        <div className="login-form">
          <div className="form-group">
            <label>Kullanıcı Adı</label>
            <div className="input-wrapper">
              <User className="input-icon" />
              <input
                type="email"
                placeholder="Kullanıcı adınızı girin"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Şifre</label>
            <div className="input-wrapper">
              <Lock className="input-icon" />
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="Şifrenizi girin"
                value={password}
                onChange={e => setPassword(e.target.value)}
                className="form-input"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="password-toggle"
              >
                {showPassword ? <EyeOff className="toggle-icon" /> : <Eye className="toggle-icon" />}
              </button>
            </div>
          </div>

          <button
            onClick={handleLogin}
            disabled={!email || !password}
            className="login-button"
          >
            <Lock className="button-icon" />
            Giriş Yap
          </button>
        </div>

        {message && (
          <div className="error-message">
            <p>{message}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Login;