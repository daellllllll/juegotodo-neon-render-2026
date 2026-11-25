import React from 'react';
import { Routes, Route } from 'react-router-dom';
import NavBar from './components/organisms/Navbar';
import Footer from './components/organisms/Footer';
import Home from './pages/Home';
import Products from './pages/Products';
import ProductDetail from './pages/ProductDetail';
import Contacto from './pages/contacto';
import Login from './pages/Login';
import Favoritos from './pages/Favoritos';
import Ofertas from './pages/Ofertas';
import Admin from './pages/Admin';
import ProtectedRoute from './auth/ProtectedRoute';
import './styles/app.css';

function App() {
  return (
    <div className="app-container">
      <NavBar />

      <main className="app-main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/products" element={<Products />} />
          <Route path="/products/:id" element={<ProductDetail />} />
          <Route path="/contacto" element={<Contacto />} />
          <Route path="/login" element={<Login />} />
          <Route path="/ofertas" element={<Ofertas />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/favoritos" element={<Favoritos />} />
          </Route>
          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin" element={<Admin />} />
          </Route>
        </Routes>
      </main>

      <Footer />
    </div>
  );
}

export default App;
