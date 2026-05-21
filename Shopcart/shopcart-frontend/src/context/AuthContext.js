import { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState('');
  const [role, setRole] = useState('');

  // Load auth info from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedRole = localStorage.getItem('role');
    if (storedToken) setToken(storedToken);
    if (storedRole) setRole(storedRole);
  }, []);

  const login = (token, role) => {
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    setToken(token);
    setRole(role);
  };

  const logout = () => {
    localStorage.clear();
    setToken('');
    setRole('');
  };

  return (
    <AuthContext.Provider value={{ token, role, login, logout }}>
      {children}
      
    </AuthContext.Provider>
  );
};
