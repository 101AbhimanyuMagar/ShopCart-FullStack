import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Products = () => {
  const [products, setProducts] = useState([]);
  const { token } = useContext(AuthContext);

  const addToCart = async (productId) => {
    try {
      await axios.post(
        `http://localhost:8080/api/cart/add?productId=${productId}&quantity=1`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("Added to cart!");
    } catch {
      alert("Login required to add items to cart.");
    }
  };

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/products")
      .then((res) => setProducts(res.data))
      .catch((err) => console.error(err));
  }, []);

  return (
    <div className="container mt-5">
      <h2 className="mb-4">Featured Products</h2>
      <div className="row g-4">
        {products.map((product) => (
          <div key={product.id} className="col-12 col-sm-6 col-md-4 col-lg-3">
            <div className="card h-100 shadow-sm border-0 rounded-4 d-flex flex-column">
              
              {/* For Sale Badge */}
              <div className="position-absolute bg-danger text-white px-3 py-1 rounded-end" style={{ top: '10px', left: '0', fontSize: '0.9rem', fontWeight: 'bold' }}>
                For Sale
              </div>

              <Link to={`/products/${product.id}`} className="text-decoration-none text-dark flex-grow-1">
                <img
                  src={product.imageUrl 
                        ? `http://localhost:8080/${product.imageUrl}`
                        : "/images/shopcart_logo.png"}
                  alt={product.name}
                  className="card-img-top rounded-top-4"
                  loading="lazy"
                  style={{
                    height: "220px",      // Fixed height
                    objectFit: "cover",   // Maintain crop look
                    width: "100%"         // Full width
                  }}
                />
                <div className="card-body d-flex flex-column">
                  <h5 className="card-title">{product.name}</h5>
                  <p className="text-muted fw-semibold">₹{product.price}</p>
                  <div className="mt-auto">
                    <p className="text-success fw-bold mb-0">Available</p>
                  </div>
                </div>
              </Link>

              <div className="card-footer bg-white border-0 text-center pb-3 mt-auto">
                <button
                  className="btn btn-sm btn-warning px-4 rounded-pill"
                  onClick={() => addToCart(product.id)}
                >
                  Add to Cart
                </button>
              </div>

            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Products;
