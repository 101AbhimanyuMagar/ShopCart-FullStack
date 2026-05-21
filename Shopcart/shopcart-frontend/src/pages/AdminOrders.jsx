import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import API from "../api/axios";

const AdminOrders = () => {
  const { token, role } = useContext(AuthContext);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchOrders = async () => {
    if (!token || (role !== "ADMIN" && role !== "SUPER_ADMIN")) {
      toast.error("❌ You are not authorized to view this page.");
      return;
    }

    try {
      const res = await API.get("/orders/admin", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setOrders(res.data);
    } catch (err) {
      console.error("Order fetch error:", err);
      toast.error("⚠️ Failed to fetch orders.");
    } finally {
      setLoading(false);
    }
  };

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      const res = await API.put(`/orders/${orderId}/status?status=${newStatus}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });

      toast.success(res.data || "✅ Order status updated!");
      fetchOrders();
    } catch (err) {
      console.error("Update status error:", err);
      toast.error("⚠️ Failed to update order status.");
    }
  };

  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line
  }, []);

  if (loading) {
    return <div className="text-center mt-5">Loading orders...</div>;
  }

  if (role !== "ADMIN" && role !== "SUPER_ADMIN") {
    return (
      <p className="mt-4 text-danger">
        ❌ You are not authorized to view this page.
      </p>
    );
  }

  return (
    <div className="container mt-5">
      <h2 className="mb-4">
        {role === "SUPER_ADMIN" ? "📦 All Orders (Super Admin)" : "📦 Your Product Orders"}
      </h2>

      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="card mb-4 shadow-sm">
            <div className="card-header d-flex justify-content-between align-items-center">
              <div>
                <strong>Order #{order.id}</strong> –{" "}
                <span className="text-capitalize">{order.status}</span>
                <div className="small text-muted">
                  {order.userName} (Total: ₹{order.totalAmount})
                </div>
              </div>

              {/* Only Admin/SuperAdmin can change status */}
              <select
                value={order.status}
                onChange={(e) => updateOrderStatus(order.id, e.target.value)}
                className="form-select w-auto"
              >
                <option value="PLACED">Placed</option>
                <option value="SHIPPED">Shipped</option>
                <option value="DELIVERED">Delivered</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>

            <div className="card-body">
              <p>
                <strong>Order Date:</strong>{" "}
                {new Date(order.createdAt).toLocaleString()}
              </p>

              <div className="table-responsive">
                <table className="table table-sm">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Price (₹)</th>
                      <th>Qty</th>
                      <th>Total</th>
                      {role === "SUPER_ADMIN" && <th>Added By</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {order.orderItems.map((item) => (
                      <tr key={item.id}>
                        <td>{item.productName}</td>
                        <td>₹{item.price}</td>
                        <td>{item.quantity}</td>
                        <td>₹{item.total}</td>
                        {role === "SUPER_ADMIN" && (
                          <td>{item.addedByName || "N/A"}</td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default AdminOrders;
