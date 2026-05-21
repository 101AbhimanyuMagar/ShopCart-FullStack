import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

const AdminRoute = ({ children }) => {
  const { role, token } = useContext(AuthContext);
  
  if (!token) return <Navigate to="/login" />;
  if (role !== "ADMIN" && role !== "SUPER_ADMIN") return <Navigate to="/" />;
  
  return children;
};

export default AdminRoute;
