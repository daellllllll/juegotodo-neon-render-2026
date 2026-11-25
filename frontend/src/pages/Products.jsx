import React, { useState, useEffect } from 'react';
import { Container, Row } from 'react-bootstrap';
import { useSearchParams } from 'react-router-dom';
import ProductCard from '../components/organisms/ProductCard';
import CategoryFilterSection from '../components/organisms/CategoryFilterSection';
import Text from '../components/atoms/Text';
import { getCategories, getProducts } from '../data/products';
import { useAuth } from '../auth/AuthContext';
import '../styles/product.css';

function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const categoriaURL = searchParams.get('categoria');

  const [categoriaActiva, setCategoriaActiva] = useState(categoriaURL || null);
  const [busqueda, setBusqueda] = useState('');
  const [orden, setOrden] = useState('default');
  const [listaJuegos, setListaJuegos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const { request } = useAuth();

  useEffect(() => {
    let activo = true;
    setCargando(true);
    Promise.all([getProducts(request), getCategories(request)])
      .then(([productos, categoriasApi]) => {
        if (!activo) return;
        setListaJuegos(productos);
        setCategorias(categoriasApi);
        setError('');
      })
      .catch((errorApi) => {
        if (activo) setError(errorApi.message);
      })
      .finally(() => {
        if (activo) setCargando(false);
      });
    return () => { activo = false; };
  }, [request]);

  useEffect(() => {
    setCategoriaActiva(categoriaURL || null);
  }, [categoriaURL]);

  const seleccionarCategoria = (nombre) => {
    if (!nombre || categoriaActiva === nombre) {
      setCategoriaActiva(null);
      setSearchParams({});
    } else {
      setCategoriaActiva(nombre);
      setSearchParams({ categoria: nombre });
    }
  };

  const limpiarTodo = () => {
    setCategoriaActiva(null);
    setBusqueda('');
    setOrden('default');
    setSearchParams({});
  };

  const obtenerPrecioNumerico = (juego) => {
    const valor = juego.price ?? juego.precio ?? 0;
    if (typeof valor === 'number') return valor;
    return Number(String(valor).replace(/[^0-9]/g, '')) || 0;
  };

  const juegosProcesados = listaJuegos
    .filter((juego) => {
      const catJuego = (juego.category || juego.categoria || '').toUpperCase();
      const coincideCategoria = !categoriaActiva || catJuego === categoriaActiva.toUpperCase();

      const tituloJuego = (juego.title || juego.nombre || juego.name || '').toLowerCase();
      const coincideBusqueda = !busqueda.trim() || tituloJuego.includes(busqueda.toLowerCase().trim());

      return coincideCategoria && coincideBusqueda;
    })
    .sort((a, b) => {
      if (orden === 'precio-asc') {
        return obtenerPrecioNumerico(a) - obtenerPrecioNumerico(b);
      }
      if (orden === 'precio-desc') {
        return obtenerPrecioNumerico(b) - obtenerPrecioNumerico(a);
      }
      if (orden === 'nombre-asc') {
        const nombreA = (a.title || a.nombre || a.name || '').toLowerCase();
        const nombreB = (b.title || b.nombre || b.name || '').toLowerCase();
        return nombreA.localeCompare(nombreB);
      }
      return 0;
    });

  return (
    <Container className="my-4">
      <Text variant="h2" className="products-all-title">
        CATALOGO DE VIDEOJUEGOS
      </Text>

      <CategoryFilterSection
        categorias={categorias}
        categoriaActiva={categoriaActiva}
        onSelectCategory={seleccionarCategoria}
        busqueda={busqueda}
        onSearchChange={setBusqueda}
        orden={orden}
        onOrdenChange={setOrden}
        totalResultados={juegosProcesados.length}
        onLimpiarTodo={limpiarTodo}
      />

      {error && <p role="alert" className="text-center text-danger">{error}</p>}
      {cargando && <p role="status" className="text-center">Cargando catálogo...</p>}

      {!cargando && !error && juegosProcesados.length > 0 ? (
        <Row>
          {juegosProcesados.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </Row>
      ) : (!cargando && !error) ? (
        <div className="empty-catalog-box">
          <p className="empty-catalog-title">No se encontraron videojuegos</p>
          <p className="empty-catalog-subtitle">
            Intenta con otro nombre o limpia los filtros activos.
          </p>
          <button type="button" className="btn-sesion-neon" onClick={limpiarTodo}>
            VER TODOS LOS JUEGOS
          </button>
        </div>
      ) : null}
    </Container>
  );
}

export default Products;
