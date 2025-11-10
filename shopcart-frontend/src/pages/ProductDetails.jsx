import React, { useEffect, useState, useContext } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';

const ProductDetails = () => {
  const { id } = useParams();
  const { token } = useContext(AuthContext);

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    axios
      .get(`http://localhost:8080/api/products/${id}`)
      .then((res) => {
        setProduct(res.data);
        setQuantity(1);
      })
      .catch((err) => console.error(err));
  }, [id]);

  const addToCart = async () => {
    try {
      await axios.post(
        `http://localhost:8080/api/cart/add?productId=${product.id}&quantity=${quantity}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("Added to cart!");
    } catch {
      alert("Login required to add items to cart.");
    }
  };

  if (!product) return <div className="text-center mt-5">Loading...</div>;

  return (
    <div className="container mt-5">
      <div className="row g-5 align-items-start">
        <div className="col-md-6 text-center">
          <img
  src={product.imageUrl 
        ? `http://localhost:8080/${product.imageUrl}` 
        : "/images/shopcart_logo.png"}
  alt={product.name}
  className="img-fluid rounded-4 shadow-sm"
  style={{ maxHeight: "400px", objectFit: "contain" }}
/>

        </div>
        <div className="col-md-6">
          <h2 className="fw-bold">{product.name}</h2>
          <h4 className="text-warning mb-3">₹{product.price}</h4>
          <p className="text-muted">{product.description}</p>
          <p><strong>In Stock:</strong> {product.stock}</p>

          <div className="mb-3 mt-4">
            <label htmlFor="quantity" className="form-label fw-semibold">Quantity:</label>
            <input
              type="number"
              className="form-control w-50 rounded-3"
              id="quantity"
              value={quantity}
              min={1}
              max={product.stock}
              onChange={(e) =>
                setQuantity(
                  Math.min(product.stock, Math.max(1, parseInt(e.target.value) || 1))
                )
              }
            />
          </div>

          <button
            className="btn btn-warning btn-lg px-5 rounded-pill mt-3"
            onClick={addToCart}
            disabled={quantity < 1 || quantity > product.stock}
          >
            Add to Cart
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetails;
