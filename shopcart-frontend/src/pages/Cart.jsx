import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

const Cart = () => {
  const { token } = useContext(AuthContext);
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCartItems = async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/cart", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setCartItems(res.data);
      } catch {
        toast.error("⚠ Please login to view cart.");
      } finally {
        setLoading(false);
      }
    };
    fetchCartItems();
  }, [token]);

  const removeItem = async (cartItemId) => {
    if (!window.confirm("Remove this item?")) return;
    await axios.delete(`http://localhost:8080/api/cart/${cartItemId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setCartItems((prev) => prev.filter((i) => i.id !== cartItemId));
    toast.success("✅ Item removed.");
  };

  const grandTotal = cartItems.reduce((sum, i) => sum + i.total, 0);

  if (loading) return <div className="text-center mt-5">Loading...</div>;

  return (
    <div className="container mt-5">
      <h2>🛒 Your Cart</h2>
      {cartItems.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <table className="table table-bordered mt-3">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Qty</th>
                <th>Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cartItems.map((item) => (
                <tr key={item.id}>
                  <td>{item.product.name}</td>
                  <td>₹{item.product.price}</td>
                  <td>{item.quantity}</td>
                  <td>₹{item.total}</td>
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
                <td colSpan="3" className="text-end fw-bold">
                  Grand Total:
                </td>
                <td className="fw-bold">₹{grandTotal}</td>
                <td></td>
              </tr>
            </tbody>
          </table>

          <div className="text-end">
            <button
  className="btn btn-warning"
  onClick={() => navigate("/checkout", { state: { cartItems, grandTotal } })}
>
  Proceed to Checkout →
</button>

          </div>
        </>
      )}
    </div>
  );
};

export default Cart;
