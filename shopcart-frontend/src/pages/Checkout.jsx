import React, { useState, useContext } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { toast } from "react-toastify";

const Checkout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { token } = useContext(AuthContext);

  const cartItems = location.state?.cartItems || [];
  const grandTotal = location.state?.grandTotal || 0;

  // ✅ Matches backend ShippingAddress fields
  const [address, setAddress] = useState({
    street: "",
    city: "",
    state: "",
    zipCode: "",
    country: "",
  });

  const [isPaying, setIsPaying] = useState(false);

  const handleAddressChange = (e) => {
    setAddress({ ...address, [e.target.name]: e.target.value });
  };

  // ✅ Updated validation for new fields
  const isAddressValid = () => {
    return (
      address.street.trim() &&
      address.city.trim() &&
      address.state.trim() &&
      address.zipCode.trim() &&
      address.country.trim()
    );
  };

  const simulatePayment = async (method) => {
    setIsPaying(true);
    await new Promise((res) => setTimeout(res, 1500));
    const success = Math.random() < 0.9; // 90% success rate
    if (success) {
      const txnId = "TXN-" + Date.now();
      toast.success(`💳 ${method} Payment Success! TXN: ${txnId}`);
      return { status: "SUCCESS", txnId };
    } else {
      toast.error("❌ Payment Failed!");
      return { status: "FAILED" };
    }
  };

  const placeOrder = async (method) => {
    if (!isAddressValid()) {
      toast.warn("Please fill in all shipping address fields!");
      return;
    }

    const payment = await simulatePayment(method);
    if (payment.status !== "SUCCESS") {
      setIsPaying(false);
      return;
    }

    const orderRequest = {
      shippingAddress: address, // ✅ same object as backend expects
      paymentMethod: method,
      totalAmount: grandTotal,
      transactionId: payment.txnId,
    };

    try {
      await axios.post("http://localhost:8080/api/orders/place", orderRequest, {
        headers: { Authorization: `Bearer ${token}` },
      });
      toast.success("🎉 Order placed successfully!");
      navigate("/orders");
    } catch (err) {
      toast.error("❌ Failed to place order!");
      console.error(err);
    } finally {
      setIsPaying(false);
    }
  };

  return (
    <div className="container mt-5">
      <h2>🧾 Checkout</h2>

      {/* 🛍️ Order Summary */}
      <div className="card mt-3 p-3">
        <h5>🛍️ Order Summary</h5>
        <table className="table table-sm mt-2">
          <thead>
            <tr>
              <th>Product</th>
              <th>Qty</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {cartItems.map((item) => (
              <tr key={item.id}>
                <td>{item.product.name}</td>
                <td>{item.quantity}</td>
                <td>₹{item.total}</td>
              </tr>
            ))}
            <tr>
              <td colSpan="2" className="text-end fw-bold">
                Grand Total:
              </td>
              <td className="fw-bold">₹{grandTotal}</td>
            </tr>
          </tbody>
        </table>
      </div>

      {/* 🏠 Shipping Address */}
      <h5 className="mt-3">Shipping Address</h5>
      <div className="row mt-2">
        {["street", "city", "state", "zipCode", "country"].map((field) => (
          <div className="col-md-6 mb-3" key={field}>
            <input
              type="text"
              name={field}
              className="form-control"
              placeholder={
                field === "zipCode"
                  ? "Zip Code"
                  : field.charAt(0).toUpperCase() + field.slice(1)
              }
              value={address[field]}
              onChange={handleAddressChange}
            />
          </div>
        ))}
      </div>

      {/* 💳 Payment Options */}
      <h5 className="mt-4">Choose Payment Method:</h5>
      <div className="mt-2">
        <button
          className="btn btn-success me-2"
          disabled={isPaying}
          onClick={() => placeOrder("CARD")}
        >
          💳 Pay by Card
        </button>
        <button
          className="btn btn-primary me-2"
          disabled={isPaying}
          onClick={() => placeOrder("UPI")}
        >
          📱 Pay by UPI
        </button>
        <button
          className="btn btn-secondary"
          disabled={isPaying}
          onClick={() => placeOrder("COD")}
        >
          🏠 Cash on Delivery
        </button>
      </div>
    </div>
  );
};

export default Checkout;
