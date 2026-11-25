import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import { addFavorite, getFavoriteProducts, getProductById, removeFavorite } from '../data/products';
import { useAuth } from '../auth/AuthContext';
import ProductDetailNotFound from '../components/organisms/ProductDetailNotFound';
import ProductDetailCard from '../components/organisms/ProductDetailCard';
import PriceComparatorSection from '../components/organisms/PriceComparatorSection';
import '../styles/productDetail.css';

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario, request } = useAuth();
  const [product, setProduct] = useState(null);
  const [esFavorito, setEsFavorito] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargarProducto = useCallback(async () => {
    setCargando(true);
    try {
      const producto = await getProductById(id, request);
      setProduct(producto);
      setError('');
      if (usuario) {
        const favoritos = await getFavoriteProducts(request);
        setEsFavorito(favoritos.some((favorito) => favorito.id === producto.id));
      } else {
        setEsFavorito(false);
      }
    } catch (errorApi) {
      setProduct(null);
      setError(errorApi.message);
    } finally {
      setCargando(false);
    }
  }, [id, request, usuario]);

  useEffect(() => {
    cargarProducto();
  }, [cargarProducto]);

  const toggleFavorito = async () => {
    if (!usuario) {
      navigate('/login', { state: { from: { pathname: `/products/${id}` } } });
      return;
    }

    try {
      if (esFavorito) {
        await removeFavorite(product.id, request);
        setEsFavorito(false);
      } else {
        await addFavorite(product.id, request);
        setEsFavorito(true);
      }
      window.dispatchEvent(new Event('favoritosActualizados'));
      setError('');
    } catch (errorApi) {
      setError(errorApi.message);
    }
  };

  if (cargando) {
    return <p className="text-center py-5" role="status">Cargando videojuego...</p>;
  }
  if (!product) {
    return (
      <Container className="my-5">
        {error && <p role="alert" className="text-center text-danger">{error}</p>}
        <ProductDetailNotFound />
      </Container>
    );
  }

  return (
    <Container className="my-5 product-detail-container">
      {error && <p role="alert" className="text-center text-danger">{error}</p>}
      <ProductDetailCard
        product={product}
        esFavorito={esFavorito}
        onToggleFavorito={toggleFavorito}
      />
      <PriceComparatorSection product={product} />
    </Container>
  );
}

export default ProductDetail;
