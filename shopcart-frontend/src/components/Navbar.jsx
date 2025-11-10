import React, { useContext } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Navbar.css';

function Navbar() {
  const { token, role, logout } = useContext(AuthContext);
  // const isAdmin = role === 'ADMIN' || role === 'SUPER_ADMIN';

  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark custom-navbar px-4">
      {/* Logo */}
      <NavLink className="navbar-brand fw-bold" to="/">
        <img src="/images/shopcart_logo.png" alt="Logo" className="navbar-logo" />
        ShopCart
      </NavLink>

      {/* Mobile Toggle */}
      <button
        className="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
        aria-controls="navbarNav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span className="navbar-toggler-icon"></span>
      </button>

      {/* Navbar Content */}
      <div className="collapse navbar-collapse" id="navbarNav">
        {/* Search in center */}
        <div className="mx-auto nav-search">
          <input type="text" placeholder="Find product" />
          <button>
            <i className="bi bi-search"></i>
          </button>
        </div>

        {/* Menu/Icons on right */}
        <ul className="navbar-nav ms-auto align-items-center">
          <li className="nav-item">
            <NavLink className="nav-link" to="/products">
              Products
            </NavLink>
          </li>

          {token ? (
  <>
    {/* Only normal USER sees Cart and Orders */}
    {role === "USER" && (
      <>
        <li className="nav-item">
          <NavLink className="nav-link icon-link" to="/cart">
            <i className="bi bi-cart3"></i> Cart
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink className="nav-link icon-link" to="/orders">
            <i className="bi bi-box-seam"></i> Orders
          </NavLink>
        </li>
      </>
    )}

    {/* ADMIN sees orders for their products and manage products */}
    {role === "ADMIN" && (
      <>
        <li className="nav-item">
          <NavLink className="nav-link admin-link" to="/admin/orders">
            Admin Orders
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink className="nav-link admin-link" to="/admin/products">
            Admin Products
          </NavLink>
        </li>
      </>
    )}

    {/* SUPER_ADMIN sees everything: metrics + full admin panel */}
    {role === "SUPER_ADMIN" && (
      <>
        <li className="nav-item">
          <NavLink className="nav-link btn admin-btn ms-2" to="/super-admin">
            Super Admin
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink className="nav-link admin-link" to="/admin">
            Admin Panel
          </NavLink>
        </li>
      </>
    )}

    {/* Logout button for all logged-in users */}
    <li className="nav-item">
      <button
        className="btn btn-outline-light btn-sm ms-3"
        onClick={handleLogout}
      >
        Logout
      </button>
    </li>
  </>
) : (
  <>
    <li className="nav-item">
      <NavLink className="nav-link icon-link" to="/login">
        <i className="bi bi-person"></i> Login
      </NavLink>
    </li>
    <li className="nav-item">
      <NavLink className="btn btn-light btn-sm ms-2" to="/register">
        Register
      </NavLink>
    </li>
  </>
)}

        </ul>
      </div>
    </nav>
  );
}

export default Navbar;
