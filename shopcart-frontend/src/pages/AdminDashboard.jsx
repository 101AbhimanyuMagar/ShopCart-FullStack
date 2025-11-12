import React, { useEffect, useState, useContext, useMemo } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { Doughnut, Bar } from "react-chartjs-2";
import CountUp from "react-countup";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
} from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

const ORANGE = "#ff7f0e";
const ORANGE_SOFT = "#ffe8cc";
const GREEN = "#2ca02c";
const BLUE = "#1f77b4";
const RED = "#d62728";


const formatINR = (n) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(n ?? 0);

export default function AdminDashboard() {
  const { token, role } = useContext(AuthContext);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const isAdmin = role === "ADMIN";
  const isSuperAdmin = role === "SUPER_ADMIN";


  useEffect(() => {
    if (!isAdmin && !isSuperAdmin) return;
    (async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/admin/metrics", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setData(res.data);
      } catch (e) {
        setErr(e?.response?.data?.message || "Failed to load metrics");
      } finally {
        setLoading(false);
      }
    })();
  }, [token, isAdmin, isSuperAdmin]);


  const {
    totalUsers = 0,
    totalOrders = 0,
    totalRevenue = 0,
    placedOrders = 0,
    shippedOrders = 0,
    deliveredOrders = 0,
    cancelledOrders = 0,
  } = data || {};

  const statusItems = useMemo(() => [
    { label: "Placed", value: placedOrders, color: BLUE },
    { label: "Shipped", value: shippedOrders, color: ORANGE },
    { label: "Delivered", value: deliveredOrders, color: GREEN },
    { label: "Cancelled", value: cancelledOrders, color: RED },
  ], [placedOrders, shippedOrders, deliveredOrders, cancelledOrders]);


  const statusTotal = Math.max(
    1,
    statusItems.reduce((a, b) => a + (Number.isFinite(b.value) ? b.value : 0), 0)
  );

  const doughnutData = useMemo(
    () => ({
      labels: statusItems.map((s) => s.label),
      datasets: [
        {
          label: "Orders",
          data: statusItems.map((s) => s.value || 0),
          backgroundColor: statusItems.map((s) => s.color),
          borderWidth: 0,
        },
      ],
    }),
    [statusItems]
  );

  const doughnutOptions = useMemo(
    () => ({
      cutout: "65%",
      plugins: {
        legend: {
          position: "bottom",
          labels: { boxWidth: 12 },
        },
        tooltip: {
          callbacks: {
            label: (ctx) =>
              `${ctx.label}: ${ctx.parsed} (${Math.round(
                (ctx.parsed / statusTotal) * 100
              )}%)`,
          },
        },
      },
      maintainAspectRatio: false,
    }),
    [statusTotal]
  );

  const barData = useMemo(
    () => ({
      labels: statusItems.map((s) => s.label),
      datasets: [
        {
          label: "Orders",
          data: statusItems.map((s) => s.value || 0),
          backgroundColor: statusItems.map((s) => s.color),
          borderRadius: 6,
        },
      ],
    }),
    [statusItems]
  );


  const barOptions = {
    plugins: {
      legend: { display: false },
      tooltip: { enabled: true },
    },
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true, grid: { color: "rgba(0,0,0,0.06)" } },
    },
  };

// Early returns come AFTER all hooks
if (!isAdmin && !isSuperAdmin) {
  return <div className="alert alert-warning">You are not authorized to view this page.</div>;
}
if (loading) {
  return <div className="text-center">Loading...</div>;
}
if (err) {
  return <div className="alert alert-danger">{err}</div>;
}


  return (
    <div className="container my-5">
      <div className="d-flex align-items-center justify-content-between mb-4">
        <h2 className="fw-bold m-0">
  {isSuperAdmin ? "Super Admin Dashboard" : "Admin Dashboard"}
</h2>

<span className="badge rounded-pill" style={{ backgroundColor: ORANGE, color: "#111" }}>
  {isSuperAdmin ? "System Metrics" : "Your Metrics"}
</span>


        
      </div>

      {/* KPI cards */}
      <div className="row g-4">
       {isSuperAdmin && (
  <div className="col-12 col-md-4">
    <div className="card border-0 shadow-sm h-100">
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          <div className="text-muted">Total Users</div>
          <div className="display-6 fw-bold">
            <CountUp start={0} end={totalUsers} duration={2} separator="," />
          </div>
        </div>
        <div
          className="rounded-circle d-flex align-items-center justify-content-center"
          style={{ width: 56, height: 56, background: ORANGE_SOFT }}
          title="Users"
        >
          <i className="bi bi-people-fill" style={{ fontSize: 22, color: ORANGE }} />
        </div>
      </div>
    </div>
  </div>
)}


        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body d-flex justify-content-between align-items-center">
              <div>
                <div className="text-muted">Total Orders</div>
                <div className="display-6 fw-bold">
                  <CountUp start={0} end={totalOrders} duration={2} separator="," />
                </div>
              </div>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: 56, height: 56, background: "#e8f0ff" }}
                title="Orders"
              >
                <i className="bi bi-bag-check-fill" style={{ fontSize: 22, color: BLUE }} />
              </div>
            </div>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body d-flex justify-content-between align-items-center">
              <div>
                <div className="text-muted">Total Revenue</div>
                <div className="display-6 fw-bold" style={{ color: ORANGE }}>
                  ₹<CountUp start={0} end={totalRevenue} duration={2.5} separator="," decimals={2} />
                </div>
              </div>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: 56, height: 56, background: "#fff3cd" }}
                title="Revenue"
              >
                <i className="bi bi-currency-rupee" style={{ fontSize: 22, color: ORANGE }} />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Charts row */}
      <div className="row g-4 mt-1">
        <div className="col-12 col-lg-5">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <h5 className="fw-bold mb-3">Order Status (Share)</h5>
              <div style={{ height: 280 }}>
                <Doughnut data={doughnutData} options={doughnutOptions} />
              </div>
              {/* Legend-like mini stats */}
              <div className="row mt-3 g-2">
                {statusItems.map((s) => (
                  <div key={s.label} className="col-6">
                    <div className="d-flex align-items-center gap-2">
                      <span
                        style={{
                          display: "inline-block",
                          width: 12,
                          height: 12,
                          background: s.color,
                          borderRadius: 2,
                        }}
                      />
                      <small className="text-muted">
                        {s.label} • {Math.round((s.value / statusTotal) * 100)}%
                      </small>
                    </div>
                    <div className="fw-semibold">{s.value}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-7">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <h5 className="fw-bold mb-3">Orders by Status</h5>
              <div style={{ height: 280 }}>
                <Bar data={barData} options={barOptions} />
              </div>
              <small className="text-muted d-block mt-2">
                Quick view of absolute order counts per status.
              </small>
            </div>
          </div>
        </div>
      </div>

      {/* Summary table */}
      <div className="card border-0 shadow-sm mt-4">
        <div className="card-body">
          <h5 className="fw-bold mb-3">Summary</h5>
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <thead style={{ backgroundColor: ORANGE, color: "white" }}>
                <tr>
                  <th>Metric</th>
                  <th className="text-end">Value</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Total Users</td>
                  <td className="text-end">{totalUsers}</td>
                </tr>
                <tr>
                  <td>Total Orders</td>
                  <td className="text-end">{totalOrders}</td>
                </tr>
                <tr>
                  <td>Total Revenue</td>
                  <td className="text-end">{formatINR(totalRevenue)}</td>
                </tr>
                <tr>
                  <td>Placed</td>
                  <td className="text-end">{placedOrders}</td>
                </tr>
                <tr>
                  <td>Shipped</td>
                  <td className="text-end">{shippedOrders}</td>
                </tr>
                <tr>
                  <td>Delivered</td>
                  <td className="text-end">{deliveredOrders}</td>
                </tr>
                <tr>
                  <td>Cancelled</td>
                  <td className="text-end">{cancelledOrders}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <small className="text-muted mt-2 d-block">
            Metrics reflect current totals. Add a revenue-by-month endpoint later to show trends.
          </small>
        </div>
      </div>
    </div>
  );
}
