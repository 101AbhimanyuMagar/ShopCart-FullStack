import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Products = () => {
  const [products, setProducts] = useState([]);
  const { token } = useContext(AuthContext);
  const [timeLeft, setTimeLeft] = useState({}); // store timers per product

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
      .then((res) => {
        setProducts(res.data);
        initializeTimers(res.data);
      })
      .catch((err) => console.error(err));
  }, []);

  // ✅ Initialize countdown timers for products with discounts
  const initializeTimers = (products) => {
    const timers = {};
    products.forEach((p) => {
      if (p.discount && p.discount.active && p.discount.endDate) {
        const endTime = new Date(p.discount.endDate).getTime();
        timers[p.id] = Math.max(0, endTime - new Date().getTime());
      }
    });
    setTimeLeft(timers);
  };

  // ✅ Countdown logic
  useEffect(() => {
    const interval = setInterval(() => {
      setTimeLeft((prev) => {
        const updated = {};
        Object.keys(prev).forEach((id) => {
          updated[id] = Math.max(0, prev[id] - 1000);
        });
        return updated;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // ✅ Convert milliseconds to hh:mm:ss
  const formatTime = (ms) => {
    const totalSeconds = Math.floor(ms / 1000);
    const hours = String(Math.floor(totalSeconds / 3600)).padStart(2, "0");
    const minutes = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, "0");
    const seconds = String(totalSeconds % 60).padStart(2, "0");
    return `${hours}:${minutes}:${seconds}`;
  };

  return (
    <div className="container mt-5">
      <h2 className="mb-4">Featured Products</h2>
      <div className="row g-4">
        {products.map((product) => {
          const discountActive = product.discount && product.discount.active;
          const discountedPrice = discountActive
            ? (product.price * (100 - product.discount.percentage)) / 100
            : product.price;

          return (
            <div key={product.id} className="col-12 col-sm-6 col-md-4 col-lg-3">
              <div className="card h-100 shadow-sm border-0 rounded-4 d-flex flex-column">

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
                      height: "220px",
                      objectFit: "cover",
                      width: "100%"
                    }}
                  />
                  <div className="card-body d-flex flex-column">
                    <h5 className="card-title">{product.name}</h5>

                    {/* ✅ Price with discount */}
                    {product.discount?.active ? (
                      <p className="text-muted fw-semibold mb-1">
                        <span style={{ textDecoration: "line-through", marginRight: "8px" }}>
                          ₹{product.originalPrice.toFixed(2)}  {/* original */}
                        </span>
                        <span className="text-danger fw-bold">
                          ₹{product.price.toFixed(2)}          {/* discounted */}
                        </span>

                      </p>
                    ) : (
                      <p className="text-muted fw-semibold">₹{product.price.toFixed(2)}</p>
                    )}


                    {/* ✅ Countdown timer */}
                    {product.discount?.active && product.discount.endDate && timeLeft[product.id] > 0 && (
                      <p className="text-danger fw-bold mb-0" style={{ fontSize: "0.85rem" }}>
                        Hurry up! Ends in {formatTime(timeLeft[product.id])}
                      </p>
                    )}


                    <div className="mt-auto">
  {product.stock > 0 ? (
    <p className="text-success fw-bold mb-0">
      In Stock: {product.stock}
    </p>
  ) : (
    <p className="text-danger fw-bold mb-0">
      Out of Stock
    </p>
  )}
</div>

                  </div>
                </Link>

                <div className="card-footer bg-white border-0 text-center pb-3 mt-auto">
                  <button
  className="btn btn-sm btn-warning px-4 rounded-pill"
  onClick={() => addToCart(product.id)}
  disabled={product.stock === 0}
>
  {product.stock === 0 ? "Out of Stock" : "Add to Cart"}
</button>

                </div>

              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Products;
