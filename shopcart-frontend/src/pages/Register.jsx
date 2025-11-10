import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; // ✅ Import
import { toast } from "react-toastify";

import './Login.css';

const Register = () => {
  const navigate = useNavigate(); // ✅ Create navigate function
const [loading, setLoading] = useState(false);
  const [user, setUser] = useState({
    name: '', email: '', password: '', role: 'USER'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/auth/register', user);
      toast.success("Registered successfully");
      navigate('/login'); // ✅ Redirect to login page after success
    } catch (err) {
    toast.error(err.response?.data?.message || "Registration failed");
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
        <div className="login-card shadow p-4 rounded-4 w-100" style={{ maxWidth: "500px" }}>
          <div className="text-center mb-4">
            <h2 className="fw-bold">Create an Account</h2>
            <p>Join ShopCart and start shopping!</p>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="form-group mb-3">
              <input
                className="form-control rounded-3"
                placeholder="Name"
                value={user.name}
                onChange={e => setUser({ ...user, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group mb-3">
              <input
                className="form-control rounded-3"
                type="email"
                placeholder="Email"
                value={user.email}
                onChange={e => setUser({ ...user, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group mb-3">
              <input
                className="form-control rounded-3"
                type="password"
                placeholder="Password"
                value={user.password}
                onChange={e => setUser({ ...user, password: e.target.value })}
                required
              />
            </div>
            <div className="form-group mb-3">
              <select
                className="form-control rounded-3"
                value={user.role}
                onChange={e => setUser({ ...user, role: e.target.value })}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <button className="btn btn-primary w-100 mb-3 rounded-3" disabled={loading} type="submit" style={{ backgroundColor: "#ff6b00", border: "none" }}>
                {loading ? "Registering..." : "Register"}
            </button>

          </form>
          <div className="text-center">
            <small>Already have an account? <a href="/login" className="text-decoration-none text-primary">Login</a></small>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
