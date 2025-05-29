import React, { useState } from 'react';
import api from './api';
import './SignIn.css'
import { AuthRequest } from './types';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Shield, User, Lock } from 'lucide-react';

const Register: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async () => {
    setMessage(null);
    if (password !== confirmPassword) {
      setMessage('Şifreler eşleşmiyor!');
      return;
    }
    try {
      const data: AuthRequest = { email, password };
      await api.post('/auth/register', data);
      setMessage('Kayıt başarılı! Giriş yapabilirsiniz.');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
      setTimeout(() => navigate('/auth/login'), 1500);
    } catch (error: any) {
      if (error.response && error.response.data) {
        setMessage(error.response.data.message || 'Kayıt başarısız.');
      } else {
        setMessage('Sunucu hatası.');
      }
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <div className="register-logo">
            <Shield className="logo-icon" />
          </div>
          <h1>Secure Transfer</h1>
          <p className="register-subtitle">Güvenli dosya paylaşım platformu</p>
        </div>

        <div className="auth-tabs">
          <Link to="/login" className="auth-tab">Giriş Yap</Link>
          <Link to="/register" className="auth-tab active">Kayıt Ol</Link>
        </div>

        <div className="divider"></div>

        <div className="register-form">
          <div className="form-section">
            <h3 className="section-title">Kullanıcı Adı</h3>
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

          <div className="divider"></div>

          <div className="form-section">
            <h3 className="section-title">Şifre</h3>
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

          <div className="divider"></div>

          <div className="form-section">
            <h3 className="section-title">Şifre Tekrarı</h3>
            <div className="input-wrapper">
              <Lock className="input-icon" />
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="Şifrenizi tekrar girin"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                className="form-input"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="password-toggle"
              >
                {showConfirmPassword ? <EyeOff className="toggle-icon" /> : <Eye className="toggle-icon" />}
              </button>
            </div>
          </div>

          <button
            onClick={handleRegister}
            disabled={!email || !password || !confirmPassword}
            className="register-button"
          >
            Kayıt Ol
          </button>
        </div>

        {message && (
          <div className={`message-box ${
            message.includes('başarılı') ? 'success' : 'error'
          }`}>
            <p>{message}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Register;