import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css'; // ✅ Import styles
// index.js or App.js
import 'bootstrap-icons/font/bootstrap-icons.css';

import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Products from './pages/Products';
import Cart from './pages/Cart';
import Orders from './pages/Orders';
import ProductDetails from './pages/ProductDetails';
import AdminRoute from './components/AdminRoute';
import AdminPanel from './pages/AdminPanel';
import AdminProducts from './pages/AdminProducts';
import AdminOrders from './pages/AdminOrders';
import AdminDashboard from './pages/AdminDashboard';
import Checkout from './pages/Checkout';
import CategoryManager from './pages/CategoryManager';

function App() {
  return (
    <>
      <Navbar />
      <div >
        <Routes>
          <Route path="/" element={<Home />} />

          {/* Admin routes */}
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminPanel />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/products"
            element={
              <AdminRoute>
                <AdminProducts />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/orders"
            element={
              <AdminRoute>
                <AdminOrders />
              </AdminRoute>
            }
          />
<Route path="/admin/categories" element={<CategoryManager />} />

          {/* Public routes */}
          <Route path="/products" element={<Products />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/checkout" element={<Checkout />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/products/:id" element={<ProductDetails />} />
          <Route path="/super-admin" element={<AdminDashboard />} />
        </Routes>
      </div>

      {/* ✅ ToastContainer must be here (outside Routes) */}
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
    </>
  );
}

export default App;
