import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";

const AdminProducts = () => {
  const { token, role } = useContext(AuthContext);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState({ name: "", description: "", price: "", stock: "" });
  const [imageFile, setImageFile] = useState(null);
  const [editingId, setEditingId] = useState(null);

  // ✅ fetchProducts defined normally
  const fetchProducts = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/products/my-products", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setProducts(res.data);
    } catch (error) {
      console.error("Error fetching admin products:", error);
    }
  };

  // ✅ useEffect at top level
  useEffect(() => {
    fetchProducts();
  }, []);




  const startDiscount = (product) => {
    setDiscountForm({
      productId: product.id,
      percentage: product.discount?.percentage || "",
      endDate: product.discount?.endDate?.split("T")[0] || "",
      isUpdate: !!product.discount?.active,
    });
  };
  const [discountForm, setDiscountForm] = useState({
    productId: null,
    percentage: "",
    endDate: "",
    isUpdate: false,
  });

  const saveDiscount = async () => {
    const { productId, percentage, endDate, isUpdate } = discountForm;
    if (!percentage || !endDate) return alert("Fill all fields");

    const percent = parseFloat(percentage);
    if (percent < 0 || percent > 100) return alert("Discount must be 0-100%");
    if (new Date(endDate) < new Date()) return alert("End date cannot be in the past");

    try {
      if (isUpdate) {
        await axios.put(
          `http://localhost:8080/api/products/${productId}/discount`,
          null, // body is null because backend expects query params
          {
            headers: { Authorization: `Bearer ${token}` },
            params: { percentage: percent, endDate } // ✅ send as query params
          }
        );
        alert("Discount updated!");
      } else {
        await axios.post(
          `http://localhost:8080/api/products/${productId}/discount`,
          null,
          {
            headers: { Authorization: `Bearer ${token}` },
            params: { percentage: percent, endDate } // ✅ send as query params
          }
        );
        alert("Discount added!");
      }
      setDiscountForm({ productId: null, percentage: "", endDate: "", isUpdate: false });
      fetchProducts();
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Error saving discount");
    }
  };



 const handleRemoveDiscount = async (productId) => {
  if (!window.confirm("Remove discount from this product?")) return;

  try {
    await axios.delete(
      `http://localhost:8080/api/products/${productId}/discount`,
      { headers: { Authorization: `Bearer ${token}` } }
    );

    alert("Discount removed!");

    // ✅ Update products state locally instead of waiting for refetch
    setProducts(products.map(p => 
      p.id === productId ? { ...p, discount: null } : p
    ));

    // Reset form if this discount was being edited
    if (discountForm.productId === productId) {
      setDiscountForm({ productId: null, percentage: "", endDate: "", isUpdate: false });
    }

  } catch (error) {
    console.error("Error removing discount:", error);
    alert(error.response?.data?.message || "Failed to remove discount");
  }
};


  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleImageChange = (e) => {
    setImageFile(e.target.files[0]);
  };

  const startEdit = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name,
      description: product.description,
      price: product.price,
      stock: product.stock,
    });
    setImageFile(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm({ name: "", description: "", price: "", stock: "" });
    setImageFile(null);
  };

  const saveProduct = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append(
      "product",
      new Blob([JSON.stringify({
        name: form.name,
        description: form.description,
        price: parseFloat(form.price),
        stock: parseInt(form.stock),
      })], { type: "application/json" })
    );

    if (imageFile) {
      formData.append("image", imageFile);
    }

    if (editingId) {
      await axios.put(
        `http://localhost:8080/api/products/${editingId}`,
        formData,
        { headers: { Authorization: `Bearer ${token}`, "Content-Type": "multipart/form-data" } }
      );
    } else {
      await axios.post(
        "http://localhost:8080/api/products",
        formData,
        { headers: { Authorization: `Bearer ${token}`, "Content-Type": "multipart/form-data" } }
      );
    }

    cancelEdit();
    fetchProducts();
  };

  const deleteProduct = async (id) => {
    if (window.confirm("Are you sure you want to delete this product?")) {
      await axios.delete(`http://localhost:8080/api/products/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchProducts();
    }
  };

  if (role !== "ADMIN") {
    return <p className="mt-4">You are not authorized to view this page.</p>;
  }

  return (
    <div className="container my-5">
      <h2 className="mb-4 fw-bold text-dark border-bottom pb-2">
        {editingId ? "✏️ Edit Product" : "➕ Add Product"}
      </h2>

      {/* Add/Edit form */}
      <form
        onSubmit={saveProduct}
        className="p-4 border rounded shadow-sm"
        style={{ backgroundColor: "white" }}
      >
        <input
          className="form-control mb-3"
          placeholder="Product Name"
          name="name"
          value={form.name}
          onChange={handleChange}
          required
        />
        <textarea
          className="form-control mb-3"
          placeholder="Description"
          name="description"
          value={form.description}
          onChange={handleChange}
          rows={3}
          required
        />
        <div className="row g-3">
          <div className="col-md-6">
            <input
              className="form-control"
              type="number"
              placeholder="Price (₹)"
              name="price"
              value={form.price}
              onChange={handleChange}
              required
            />
          </div>
          <div className="col-md-6">
            <input
              className="form-control"
              type="number"
              placeholder="Stock"
              name="stock"
              value={form.stock}
              onChange={handleChange}
              required
            />
          </div>
        </div>
        <input
          className="form-control mt-3"
          type="file"
          accept="image/*"
          onChange={handleImageChange}
        />
        <div className="d-flex gap-2 mt-4">
          <button className="btn btn-warning text-dark fw-semibold px-4" type="submit">
            {editingId ? "Update Product" : "Add Product"}
          </button>
          {editingId && (
            <button
              type="button"
              className="btn btn-secondary px-4"
              onClick={cancelEdit}
            >
              Cancel
            </button>
          )}
        </div>
      </form>

      {/* Products table */}
      <table className="table table-hover table-bordered align-middle shadow-sm rounded mt-5">
        <thead style={{ backgroundColor: "#FFA500", color: "white" }}>
          <tr>
            <th>Name</th>
            <th>Price (₹)</th>
            <th>Stock</th>
            <th>Image</th>
            <th style={{ width: "220px" }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td className="fw-semibold">{p.name}</td>
              <td>
                ₹{p.price.toFixed(2)}
                {p.discount && p.discount.active && (
                  <div className="text-success small">
                    (-{p.discount.percentage}%)
                  </div>
                )}
              </td>
              <td>{p.stock}</td>
              <td>
                {p.imageUrl && (
                  <img
                    src={`http://localhost:8080/${p.imageUrl}`}
                    alt={p.name}
                    className="rounded shadow-sm border"
                    style={{ width: "60px", height: "60px", objectFit: "cover" }}
                  />
                )}
              </td>
              <td>
                <div className="d-flex flex-column gap-2">
                  <div className="d-flex gap-2">
                    <button
                      className="btn btn-sm"
                      style={{ backgroundColor: "#FFA500", color: "white" }}
                      onClick={() => startEdit(p)}
                    >
                      ✏️ Edit
                    </button>
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => deleteProduct(p.id)}
                    >
                      🗑 Delete
                    </button>
                  </div>

                  {/* Discount section */}
                  {discountForm.productId === p.id ? (
                    <div className="d-flex gap-2 align-items-center mt-2">
                      <input
                        type="number"
                        min="0"
                        max="100"
                        placeholder="% Discount"
                        className="form-control form-control-sm"
                        style={{ width: "80px" }}
                        value={discountForm.percentage}
                        onChange={(e) => setDiscountForm({ ...discountForm, percentage: e.target.value })}
                      />
                      <input
                        type="date"
                        className="form-control form-control-sm"
                        style={{ width: "150px" }}
                        value={discountForm.endDate}
                        onChange={(e) => setDiscountForm({ ...discountForm, endDate: e.target.value })}
                      />
                      <button className="btn btn-sm btn-success" onClick={saveDiscount}>
                        💾 Save
                      </button>
                      <button
                        className="btn btn-sm btn-danger"
                        onClick={() => handleRemoveDiscount(p.id)}
                      >
                        🗑 Remove
                      </button>

                      <button
                        className="btn btn-sm btn-secondary"
                        onClick={() => setDiscountForm({ productId: null, percentage: "", endDate: "", isUpdate: false })}
                      >
                        ❌ Cancel
                      </button>
                    </div>
                  ) : (
                    <button
                      className="btn btn-sm btn-outline-primary mt-2"
                      onClick={() => startDiscount(p)}
                    >
                      {p.discount?.active ? "✏️ Update Discount" : "➕ Add Discount"}
                    </button>
                  )}



                </div>
              </td>
            </tr>
          ))}
        </tbody>

      </table>
    </div>
  );
};

export default AdminProducts;
