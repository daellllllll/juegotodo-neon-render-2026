import React, { useState, useEffect } from 'react';
import { Container, Row } from 'react-bootstrap';
import { getProducts } from '../data/products';
import { useAuth } from '../auth/AuthContext';
import ProductCard from '../components/organisms/ProductCard';
import Text from '../components/atoms/Text';
import '../styles/ofertas.css';

function Ofertas() {
  const [productosOferta, setProductosOferta] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const { request } = useAuth();

  useEffect(() => {
    let activo = true;
    getProducts(request)
      .then((todos) => {
        if (activo) setProductosOferta(todos.filter((producto) => producto.oferta));
      })
      .catch((errorApi) => {
        if (activo) setError(errorApi.message);
      })
      .finally(() => {
        if (activo) setCargando(false);
      });
    return () => { activo = false; };
  }, [request]);

  return (
    <Container className="my-4">
      <Text variant="div" className="titulo-seccion">
         OFERTAS DESTACADAS 
      </Text>
      
      <Text variant="p" className="ofertas-subtitulo">
        Compara los mejores precios con descuento seleccionados para esta semana.
      </Text>

      {error && <p role="alert" className="text-center text-danger">{error}</p>}
      {cargando && <p role="status" className="text-center">Cargando ofertas...</p>}

      {!cargando && !error && productosOferta.length === 0 ? (
        <div className="ofertas-vacio-box">
          <Text variant="h4" className="ofertas-vacio-titulo">
            No hay juegos en oferta en este momento
          </Text>
          <Text variant="p" className="ofertas-vacio-texto">
            Vuelve más tarde para revisar nuevos descuentos.
          </Text>
        </div>
      ) : (!cargando && !error) ? (
        <Row>
          {productosOferta.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </Row>
      ) : null}
    </Container>
  );
}

export default Ofertas;
