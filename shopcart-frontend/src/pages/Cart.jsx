import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// This should be in App.js or index.js once, not here
// toast.configure();

const Cart = () => {
  const { token } = useContext(AuthContext);
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [address, setAddress] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: ''
  });

  const navigate = useNavigate();

  useEffect(() => {
    const fetchCartItems = async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/cart", {
          headers: { Authorization: `Bearer ${token}` }
        });
        setCartItems(res.data);
      } catch (err) {
        toast.error("⚠ Login required to view cart.", { position: "top-right" });
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
  }, [token]);

  const removeItem = async (cartItemId) => {
    if (!window.confirm("Are you sure you want to remove this item?")) return;

    try {
      await axios.delete(`http://localhost:8080/api/cart/${cartItemId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setCartItems((prevItems) =>
        prevItems.filter((item) => item.id !== cartItemId)
      );
      toast.success("✅ Item removed from cart.", { position: "top-right" });
    } catch (err) {
      toast.error("❌ Failed to remove item. Please try again.", { position: "top-right" });
    }
  };

  const handleAddressChange = (e) => {
    setAddress({ ...address, [e.target.name]: e.target.value });
  };

  const placeOrder = async () => {
    try {
      const request = { shippingAddress: address };

      await axios.post(
        "http://localhost:8080/api/orders/place",
        request,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      toast.success("🎉 Order placed successfully!", { position: "top-center" });
      navigate("/orders");
    } catch (error) {
      toast.error("❌ Failed to place order", { position: "top-center" });
      console.error("Order error:", error);
    }
  };

  const grandTotal = cartItems.reduce((sum, item) => sum + item.total, 0);

  if (loading) return <div className="text-center mt-5">Loading cart...</div>;

  return (
    <div className="container mt-5">
      <h2 className="mb-4">🛒 Your Cart</h2>

      {cartItems.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table align-middle">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Price (₹)</th>
                  <th>Quantity</th>
                  <th>Total (₹)</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {cartItems.map(item => (
                  <tr key={item.id}>
                    <td>{item.product.name}</td>
                    <td>{item.product.price}</td>
                    <td>{item.quantity}</td>
                    <td>{item.total}</td>
                    <td>
                      <button
                        className="btn btn-sm btn-danger"
                        onClick={() => removeItem(item.id)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
                <tr>
                  <td colSpan="3" className="text-end fw-bold">Grand Total:</td>
                  <td className="fw-bold">₹{grandTotal}</td>
                  <td></td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Shipping Address Form */}
          <div className="mt-4">
            <h4>Shipping Address</h4>
            <div className="row">
              <div className="col-md-6 mb-3">
                <input
                  type="text"
                  name="street"
                  className="form-control"
                  placeholder="Street"
                  value={address.street}
                  onChange={handleAddressChange}
                  required
                />
              </div>
              <div className="col-md-6 mb-3">
                <input
                  type="text"
                  name="city"
                  className="form-control"
                  placeholder="City"
                  value={address.city}
                  onChange={handleAddressChange}
                  required
                />
              </div>
              <div className="col-md-4 mb-3">
                <input
                  type="text"
                  name="state"
                  className="form-control"
                  placeholder="State"
                  value={address.state}
                  onChange={handleAddressChange}
                  required
                />
              </div>
              <div className="col-md-4 mb-3">
                <input
                  type="text"
                  name="zipCode"
                  className="form-control"
                  placeholder="Zip Code"
                  value={address.zipCode}
                  onChange={handleAddressChange}
                  required
                />
              </div>
              <div className="col-md-4 mb-3">
                <input
                  type="text"
                  name="country"
                  className="form-control"
                  placeholder="Country"
                  value={address.country}
                  onChange={handleAddressChange}
                  required
                />
              </div>
            </div>

            <div className="text-end">
              <button className="btn btn-success mt-3" onClick={placeOrder}>
                🛒 Checkout Now
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Cart;
