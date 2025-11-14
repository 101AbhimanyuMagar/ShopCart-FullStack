import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { Spinner } from "react-bootstrap";

const ProductDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { token } = useContext(AuthContext);

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    setLoading(true);
    axios
      .get(`http://localhost:8080/api/products/${id}`)
      .then((res) => {
        setProduct(res.data);
        setQuantity(1);
      })
      .catch(() => alert("Product not found."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleQuantityChange = (value) => {
    setQuantity((prev) => Math.min(product.stock, Math.max(1, prev + value)));
  };

  const addToCart = async () => {
    if (!token) {
      alert("Please login to add products to your cart.");
      navigate("/login");
      return;
    }

    setAdding(true);
    try {
      await axios.post(
        `http://localhost:8080/api/cart/add?productId=${product.id}&quantity=${quantity}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("✅ Product added to cart successfully!");
    } catch (error) {
      console.error(error);
      alert("Something went wrong while adding to cart.");
    } finally {
      setAdding(false);
    }
  };

  if (loading)
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <Spinner animation="border" variant="warning" />
      </div>
    );

  if (!product)
    return (
      <div className="text-center mt-5 text-danger">Product not found.</div>
    );

  // 🧠 Extract discount details
  const discount = product.discount;
  const hasDiscount = discount && discount.active;
  const discountText = hasDiscount
    ? `${discount.percentage}% OFF ${
        discount.endDate ? `till ${new Date(discount.endDate).toLocaleDateString()}` : ""
      }`
    : null;

  return (
    <div className="container py-5">
      <div className="row g-5 align-items-start">
        {/* 🖼️ Image Section */}
        <div className="col-md-6 text-center">
          <div
            className="position-relative border rounded-4 shadow-sm bg-light"
            style={{ padding: "20px" }}
          >
            {hasDiscount && (
              <span
                className="badge bg-danger position-absolute top-0 start-0 m-3 fs-6"
                style={{ borderRadius: "12px", padding: "8px 12px" }}
              >
                {discountText}
              </span>
            )}
            <img
              src={
                product.imageUrl
                  ? `http://localhost:8080/${product.imageUrl}`
                  : "/images/shopcart_logo.png"
              }
              alt={product.name}
              className="img-fluid rounded-3"
              style={{
                maxHeight: "450px",
                objectFit: "contain",
                width: "100%",
              }}
            />
          </div>
        </div>

        {/* 📄 Details Section */}
        <div className="col-md-6">
          <h5 className="text-secondary mb-2">
            Category:{" "}
            <span className="fw-semibold text-dark">
              {product.category?.name || "Uncategorized"}
            </span>
          </h5>

          <h2 className="fw-bold mb-2">{product.name}</h2>

          {/* Price Section */}
          <div className="d-flex align-items-center gap-3 mb-3">
            <h3 className="text-warning fw-bold mb-0">
              ₹{product.price.toLocaleString()}
            </h3>
            {hasDiscount && (
              <>
                <span className="text-muted text-decoration-line-through fs-5">
                  ₹{product.originalPrice.toLocaleString()}
                </span>
                <span className="badge bg-success fs-6">
                  Save {discount.percentage}%
                </span>
              </>
            )}
          </div>

          {/* Description */}
          <p className="text-muted mb-4">{product.description}</p>

          {/* Stock Info */}
          <p className="mb-3">
            <strong>In Stock:</strong>{" "}
            {product.stock > 0 ? (
              <span className="text-success fw-semibold">
                {product.stock} available
              </span>
            ) : (
              <span className="text-danger">Out of stock</span>
            )}
          </p>

          {/* Quantity Selector */}
          <div className="d-flex align-items-center gap-3 mb-4">
            <button
              className="btn btn-outline-secondary rounded-circle px-3"
              onClick={() => handleQuantityChange(-1)}
              disabled={quantity <= 1}
            >
              −
            </button>
            <input
              type="number"
              value={quantity}
              readOnly
              className="form-control text-center w-25 rounded-3"
            />
            <button
              className="btn btn-outline-secondary rounded-circle px-3"
              onClick={() => handleQuantityChange(1)}
              disabled={quantity >= product.stock}
            >
              +
            </button>
          </div>

          {/* Add to Cart Button */}
          <button
            className="btn btn-warning btn-lg px-5 rounded-pill shadow-sm"
            onClick={addToCart}
            disabled={product.stock === 0 || adding}
          >
            {adding ? "Adding..." : "🛒 Add to Cart"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetails;
