import React, { useCallback, useEffect, useState } from 'react';
import { Container, Row } from 'react-bootstrap';
import { useAuth } from '../auth/AuthContext';
import { getFavoriteProducts, removeFavorite } from '../data/products';
import Text from '../components/atoms/Text';
import FavoritosEmptyState from '../components/organisms/FavoritosEmptyState';
import FavoriteCard from '../components/organisms/FavoriteCard';
import '../styles/favoritos.css';

function Favoritos() {
  const { request } = useAuth();
  const [favoritos, setFavoritos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargarFavoritos = useCallback(async () => {
    setCargando(true);
    try {
      setFavoritos(await getFavoriteProducts(request));
      setError('');
    } catch (errorApi) {
      setError(errorApi.message);
    } finally {
      setCargando(false);
    }
  }, [request]);

  useEffect(() => {
    cargarFavoritos();
  }, [cargarFavoritos]);

  const quitarDeFavoritos = async (idProducto) => {
    try {
      await removeFavorite(idProducto, request);
      setFavoritos((actuales) => actuales.filter((producto) => producto.id !== idProducto));
      window.dispatchEvent(new Event('favoritosActualizados'));
      setError('');
    } catch (errorApi) {
      setError(errorApi.message);
    }
  };

  return (
    <Container className="my-5">
      <Text variant="div" className="titulo-seccion">
        MIS JUEGOS FAVORITOS
      </Text>

      {error && <p role="alert" className="text-center text-danger">{error}</p>}
      {cargando && <p role="status" className="text-center">Cargando favoritos...</p>}
      {!cargando && !error && favoritos.length === 0 && <FavoritosEmptyState />}
      {!cargando && !error && favoritos.length > 0 && (
        <Row>
          {favoritos.map((product) => (
            <FavoriteCard
              key={product.id}
              product={product}
              onQuitar={quitarDeFavoritos}
            />
          ))}
        </Row>
      )}
    </Container>
  );
}

export default Favoritos;
