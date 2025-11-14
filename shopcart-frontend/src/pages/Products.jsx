import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [sortOption, setSortOption] = useState("default");
  const { token } = useContext(AuthContext);
  const [timeLeft, setTimeLeft] = useState({});

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

  // Fetch products and categories
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/products");
        setProducts(res.data);
        setFilteredProducts(res.data);
        initializeTimers(res.data);

        // Extract categories from products
        // When fetching categories
        // ✅ Extract categories from products and set state
        const cats = ["All", ...new Set(res.data.map(p => p.category?.name || "Uncategorized"))];
        setCategories(cats);




      } catch (err) {
        console.error(err);
      }
    };
    fetchProducts();
  }, []);

  const initializeTimers = (products) => {
    const timers = {};
    products.forEach((p) => {
      if (p.discount?.active && p.discount.endDate) {
        const endTime = new Date(p.discount.endDate).getTime();
        timers[p.id] = Math.max(0, endTime - new Date().getTime());
      }
    });
    setTimeLeft(timers);
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setTimeLeft(prev => {
        const updated = {};
        Object.keys(prev).forEach(id => {
          updated[id] = Math.max(0, prev[id] - 1000);
        });
        return updated;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  const formatTime = (ms) => {
    const totalSeconds = Math.floor(ms / 1000);
    const hours = String(Math.floor(totalSeconds / 3600)).padStart(2, "0");
    const minutes = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, "0");
    const seconds = String(totalSeconds % 60).padStart(2, "0");
    return `${hours}:${minutes}:${seconds}`;
  };

  // Filter & Sort
  useEffect(() => {
    let temp = [...products];

    // Search filter
    if (searchQuery.trim()) {
      temp = temp.filter(p =>
        p.name.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Category filter
    if (selectedCategory !== "All") {
      temp = temp.filter(p => p.category?.name === selectedCategory);
    }


    // Sorting
    if (sortOption === "priceLowHigh") temp.sort((a, b) => a.price - b.price);
    if (sortOption === "priceHighLow") temp.sort((a, b) => b.price - a.price);

    setFilteredProducts(temp);
  }, [searchQuery, selectedCategory, sortOption, products]);

  return (
    <div className="container-fluid my-2 px-4">
      <h3
        className="fw-bold mb-2 text-center"
        style={{ color: '#ff8c00' }}
      >
        Shop Our Latest Products
      </h3>





      {/* ✅ Modern Search & Filter Bar */}
      <div className="filter-bar bg-light rounded-4 shadow-sm p-3 mb-4">
        <div className="row g-2 align-items-center">
          {/* 🔍 Search */}
          <div className="col-md-5">
            <div className="input-group shadow-sm rounded-pill">
              <span className="input-group-text bg-white border-0">
                <i className="bi bi-search text-muted"></i>
              </span>
              <input
                type="text"
                className="form-control border-0 rounded-end-pill"
                placeholder="Search for mobiles, laptops, accessories..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* 🏷️ Category */}
          <div className="col-md-3">
            <select
              className="form-select shadow-sm rounded-pill"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              {categories.map((cat, index) => (
                <option key={index} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          {/* ↕️ Sort */}
          <div className="col-md-2">
            <select
              className="form-select shadow-sm rounded-pill"
              value={sortOption}
              onChange={(e) => setSortOption(e.target.value)}
            >
              <option value="default">Sort by</option>
              <option value="priceLowHigh">Price: Low → High</option>
              <option value="priceHighLow">Price: High → Low</option>
            </select>
          </div>

          {/* ❌ Clear Button */}
          <div className="col-md-2">
            <button
              className="btn btn-outline-danger w-100 rounded-pill shadow-sm"
              onClick={() => {
                setSearchQuery("");
                setSelectedCategory("All");
                setSortOption("default");
              }}
            >
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* 🛒 Product Grid */}
      <div className="row g-4">
        {filteredProducts.length > 0 ? (
          filteredProducts.map((product) => {
            const discountActive = product.discount?.active;
            return (
              <div key={`${product.id}-${product.name}`} className="col-12 col-sm-6 col-md-4 col-lg-3 col-xl-2">
                <div className="card h-100 border-0 rounded-4 shadow-sm product-card hover-zoom">
                  {/* Discount/Tag */}
                  <div
                    className="position-absolute bg-danger text-white px-3 py-1 rounded-end"
                    style={{ top: "10px", left: "0", fontSize: "0.9rem", fontWeight: "bold" }}
                  >
                    {discountActive ? "On Sale" : "New"}
                  </div>

                  <Link to={`/products/${product.id}`} className="text-decoration-none text-dark flex-grow-1">
                    <img
                      src={
                        product.imageUrl
                          ? `http://localhost:8080/${product.imageUrl}`
                          : "/images/shopcart_logo.png"
                      }
                      alt={product.name}
                      className="card-img-top rounded-top-4"
                      style={{ height: "220px", objectFit: "cover", width: "100%" }}
                    />
                    <div className="card-body d-flex flex-column">
                      <h6 className="fw-bold text-dark">{product.name}</h6>

                      {discountActive ? (
                        <p className="text-muted fw-semibold mb-1">
                          <span
                            style={{
                              textDecoration: "line-through",
                              marginRight: "8px",
                              color: "#888",
                            }}
                          >
                            ₹{product.originalPrice.toFixed(2)}
                          </span>
                          <span className="text-danger fw-bold">₹{product.price.toFixed(2)}</span>
                        </p>
                      ) : (
                        <p className="text-dark fw-semibold mb-1">₹{product.price.toFixed(2)}</p>
                      )}

                      {discountActive && product.discount.endDate && timeLeft[product.id] > 0 && (
                        <p className="text-danger fw-bold mb-0" style={{ fontSize: "0.85rem" }}>
                          ⏰ Ends in {formatTime(timeLeft[product.id])}
                        </p>
                      )}

                      <div className="mt-auto">
                        {product.stock > 0 ? (
                          <p className="text-success fw-bold mb-0">In Stock: {product.stock}</p>
                        ) : (
                          <p className="text-danger fw-bold mb-0">Out of Stock</p>
                        )}
                      </div>
                    </div>
                  </Link>

                  <div className="card-footer bg-white border-0 text-center pb-3">
                    <button
                      className="btn btn-warning btn-sm px-4 rounded-pill shadow-sm"
                      onClick={() => addToCart(product.id)}
                      disabled={product.stock === 0}
                    >
                      {product.stock === 0 ? "Out of Stock" : "🛒 Add to Cart"}
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        ) : (
          <p className="text-center text-muted">No products found</p>
        )}
      </div>
    </div>
  );

};

export default Products;
