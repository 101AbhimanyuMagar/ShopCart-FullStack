import React, { useState, useContext } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const { login } = useContext(AuthContext);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        password
      });

      login(res.data.token, res.data.role);

      toast.success(`✅ Welcome back! Logged in as ${res.data.role}`, {
        position: 'top-right',
        autoClose: 3000
      });

      if (res.data.role === 'SUPER_ADMIN') {
        navigate('/admin');
      }
      else if(res.data.role === 'ADMIN' ){
        navigate('/admin/products');
      } else {
        navigate('/');
      }
    } catch (err) {
      const message =
        err.response?.data?.message || '❌ Login failed. Please try again.';
      toast.error(message, { position: 'top-right', autoClose: 4000 });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page rounded-2">
      <div className="login-left">
        <h1>Simplify management with our dashboard.</h1>
        <p>Manage your e-commerce easily with our user-friendly platform.</p>
        <img src="/images/shopcart_logo.png" alt="E-commerce" />
      </div>

      <div className="login-right">
        <div
          className="login-card shadow p-4 rounded-4 w-100"
          style={{ maxWidth: '500px' }}
        >
          <div className="text-center mb-4">
            <h2 className="fw-bold">Welcome Back</h2>
            <p>Please login to your account</p>
          </div>

          <form onSubmit={handleLogin}>
            <div className="form-group mb-3">
              <input
                type="email"
                className="form-control rounded-3"
                placeholder="Email address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={loading}
              />
            </div>
            <div className="form-group mb-3">
              <input
                type="password"
                className="form-control rounded-3"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={loading}
              />
            </div>
            <div className="d-flex justify-content-end mb-3">
              <a
                href="/forgot-password"
                className="text-decoration-none small"
              >
                Forgot password?
              </a>
            </div>
            <button
              className="btn btn-primary w-100 mb-3 rounded-3"
              type="submit"
              style={{ backgroundColor: '#ff6b00', border: 'none' }}
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <div className="text-center mb-3 text-muted">Or login with</div>
          <div className="d-flex gap-2 mb-3">
            <button
              className="btn btn-outline-dark w-50 rounded-3"
              disabled={loading}
            >
              <i className="bi bi-google me-1"></i> Google
            </button>
            <button
              className="btn btn-outline-dark w-50 rounded-3"
              disabled={loading}
            >
              <i className="bi bi-facebook me-1"></i> Facebook
            </button>
          </div>

          <div className="text-center">
            <small>
              Don’t have an account?{' '}
              <a
                href="/register"
                className="text-decoration-none text-primary"
              >
                Signup
              </a>
            </small>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
