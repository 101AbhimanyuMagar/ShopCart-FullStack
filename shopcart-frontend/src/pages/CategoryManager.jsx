import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";

export default function CategoryManager() {
  const { token, role } = useContext(AuthContext);
  const [categories, setCategories] = useState([]);
  const [newCategory, setNewCategory] = useState("");
  const [editCategory, setEditCategory] = useState(null);

  const isSuperAdmin = role === "SUPER_ADMIN";

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/categories", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCategories(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const addCategory = async () => {
    if (!newCategory.trim()) return;
    try {
      await axios.post(
        "http://localhost:8080/api/categories",
        { name: newCategory },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setNewCategory("");
      fetchCategories();
    } catch (err) {
      alert("Failed to add category");
    }
  };

  const updateCategory = async (id, name) => {
    try {
      await axios.put(
        `http://localhost:8080/api/categories/${id}`,
        { name },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setEditCategory(null);
      fetchCategories();
    } catch (err) {
      alert("Failed to update category");
    }
  };

  const deleteCategory = async (id) => {
    if (!window.confirm("Delete this category?")) return;
    try {
      await axios.delete(`http://localhost:8080/api/categories/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchCategories();
    } catch (err) {
      alert("Failed to delete category");
    }
  };

  if (!isSuperAdmin) {
    return <div className="alert alert-warning">Only Super Admin can manage categories.</div>;
  }

  return (
    <div className="container mt-5">
      <h2 className="fw-bold mb-4">Manage Categories</h2>

      {/* Add category form */}
      <div className="d-flex mb-4">
        <input
          type="text"
          className="form-control me-2"
          placeholder="Enter new category name"
          value={newCategory}
          onChange={(e) => setNewCategory(e.target.value)}
        />
        <button className="btn btn-primary" onClick={addCategory}>
          Add
        </button>
      </div>

      {/* Category table */}
      <div className="card shadow-sm">
        <div className="card-body">
          <table className="table table-striped align-middle">
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th style={{ width: 200 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat, idx) => (
                <tr key={cat.id}>
                  <td>{idx + 1}</td>
                  <td>
                    {editCategory?.id === cat.id ? (
                      <input
                        type="text"
                        value={editCategory.name}
                        onChange={(e) =>
                          setEditCategory({ ...editCategory, name: e.target.value })
                        }
                        className="form-control"
                      />
                    ) : (
                      cat.name
                    )}
                  </td>
                  <td>
                    {editCategory?.id === cat.id ? (
                      <>
                        <button
                          className="btn btn-success btn-sm me-2"
                          onClick={() => updateCategory(cat.id, editCategory.name)}
                        >
                          Save
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => setEditCategory(null)}
                        >
                          Cancel
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          className="btn btn-outline-primary btn-sm me-2"
                          onClick={() => setEditCategory(cat)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-outline-danger btn-sm"
                          onClick={() => deleteCategory(cat.id)}
                        >
                          Delete
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
