import React, { useEffect, useState, useContext, useCallback, useRef } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { toast } from "react-toastify";
import { motion } from "framer-motion";

const statusSteps = [
  { label: "Placed", icon: "📦" },
  { label: "Shipped", icon: "🚚" },
  { label: "Delivered", icon: "✅" },
];

const Orders = () => {
  const { token } = useContext(AuthContext);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const fetchedOnce = useRef(false);

  const fetchOrders = useCallback(async () => {
  try {
    if (!token) {
      toast.error("Please login first.");
      return;
    }

    const res = await axios.get("http://localhost:8080/api/orders", {
      headers: { Authorization: `Bearer ${token}` },
    });

    console.log("Orders fetched from backend:", res.data); // ✅ log all data
    setOrders(res.data);
  } catch (err) {
    console.error("Order fetch error:", err);
    toast.error("Failed to fetch orders.");
  } finally {
    setLoading(false);
  }
}, [token]);


  useEffect(() => {
    if (!fetchedOnce.current) {
      fetchOrders();
      fetchedOnce.current = true;
    }
  }, [fetchOrders]);

  const getStepIndex = (status) => {
    switch (status?.toLowerCase()) {
      case "placed":
        return 0;
      case "shipped":
        return 1;
      case "delivered":
        return 2;
      default:
        return -1;
    }
  };

  const capitalize = (str) => str?.charAt(0).toUpperCase() + str?.slice(1).toLowerCase();

  if (loading) return <div className="text-center mt-5">Loading orders...</div>;

  return (
    <div className="container mt-5">
      <h2 className="mb-4">📦 Your Orders</h2>

      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="card mb-4 shadow border-0 rounded-3">
            <div className="card-header bg-light fw-bold">
              Order #{order.id}
              <div className="small text-muted">
                {new Date(order.createdAt).toLocaleString()}
              </div>
            </div>

            <div className="card-body">
              {order.orderItems.map((item) => {
                const statusLower = item.status?.toLowerCase();
                const currentStep = getStepIndex(item.status);
                const isCancelled = statusLower === "cancelled";

                return (
                  <div
                    key={item.id}
                    className="border rounded-3 p-3 mb-4 shadow-sm bg-white"
                  >
                    <div className="d-flex justify-content-between align-items-center">
                      <div>
                        <h6 className="fw-bold mb-1">{item.productName}</h6>
                        <p className="mb-1">
                          ₹{item.price} × {item.quantity} = ₹{item.total}
                        </p>
                      </div>
                      <span
                        className={`badge ${
                          isCancelled
                            ? "bg-danger"
                            : statusLower === "delivered"
                            ? "bg-success"
                            : "bg-warning text-dark"
                        }`}
                      >
                        {capitalize(item.status)}
                      </span>
                    </div>

                    {!isCancelled && (
                      <div className="position-relative my-4" style={{ height: 70, overflow: "visible" }}>
                        {/* Base gray line */}
                        <div
                          style={{
                            position: "absolute",
                            top: "50%",
                            left: 0,
                            transform: "translateY(-50%)",
                            width: "100%",
                            height: 6,
                            backgroundColor: "#e0e0e0",
                            borderRadius: 4,
                            zIndex: 0,
                          }}
                        ></div>

                        {/* Animated progress bar */}
                        <motion.div
                          key={item.id + "-" + item.status}
                          initial={{ width: 0 }}
                          animate={{
                            width:
                              currentStep === 0
                                ? "33%"
                                : currentStep === 1
                                ? "66%"
                                : currentStep === 2
                                ? "100%"
                                : "0%",
                          }}
                          transition={{ duration: 1.2, ease: "easeInOut" }}
                          style={{
                            position: "absolute",
                            top: "50%",
                            left: 0,
                            transform: "translateY(-50%)",
                            height: 6,
                            background: "linear-gradient(90deg, #ff6f00, #ff9800, #ffc107)",
                            borderRadius: 4,
                            zIndex: 1,
                          }}
                        ></motion.div>

                        {/* Step icons */}
                        <div className="d-flex justify-content-between align-items-center position-relative mt-3">
                          {statusSteps.map((step, index) => (
                            <div key={index} className="text-center" style={{ width: "33%", zIndex: 2 }}>
                              <motion.div
                                animate={{ scale: index === currentStep ? [1, 1.3, 1] : 1 }}
                                transition={{ duration: 0.5 }}
                                className={`rounded-circle d-flex align-items-center justify-content-center mx-auto mb-2 ${
                                  index <= currentStep ? "bg-success text-white" : "bg-secondary text-light"
                                }`}
                                style={{
                                  width: 40,
                                  height: 40,
                                  fontSize: 20,
                                  boxShadow: index <= currentStep ? "0 0 10px rgba(0,128,0,0.4)" : "none",
                                }}
                              >
                                {step.icon}
                              </motion.div>
                              <small className={`fw-semibold ${index <= currentStep ? "text-success" : "text-muted"}`}>
                                {step.label}
                              </small>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                  </div>
                );
              })}

              <p className="fw-bold text-end fs-5 mb-0">
                Total: ₹{order.totalAmount}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default Orders;
