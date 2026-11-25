import React, { useState, useEffect, useCallback } from 'react';
import { Offcanvas } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { getFavoriteProducts } from '../../data/products';

function NavBar() {
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [cantidadFavs, setCantidadFavs] = useState(0);
  const { usuario, isAdmin, loading, request, logout } = useAuth();

  const actualizarContadorFavs = useCallback(async () => {
    if (!usuario) {
      setCantidadFavs(0);
      return;
    }
    try {
      const favoritos = await getFavoriteProducts(request);
      setCantidadFavs(favoritos.length);
    } catch {
      setCantidadFavs(0);
    }
  }, [request, usuario]);

  useEffect(() => {
    actualizarContadorFavs();

    window.addEventListener('favoritosActualizados', actualizarContadorFavs);
    return () => window.removeEventListener('favoritosActualizados', actualizarContadorFavs);
  }, [actualizarContadorFavs]);

  const cerrarSesion = async (e) => {
    e.preventDefault();
    try {
      await logout();
    } catch {
      // El contexto limpia la sesión local aunque el servidor no responda.
    }
  };

  const cerrarMenu = () => setMostrarMenu(false);

  return (
    <>
      <header className="juegotodo-header">
        <div className="header-col-izq">
          <button
            onClick={() => setMostrarMenu(true)}
            className="btn-hamburguesa"
            aria-label="Abrir menú"
          >
            <span></span>
            <span></span>
            <span></span>
          </button>
        </div>

        <div className="header-col-centro">
          <Link to="/">
            <img 
              src="/img/logopag.png" 
              alt="Logo Juegotodo" 
              className="logo-header-central"
              onError={(e) => { e.target.style.display = 'none'; }} 
            />
          </Link>
        </div>

        <div className="header-col-der">
          <Link to="/favoritos" className="btn-sesion-neon" title="Mis Favoritos">
            JUEGOS GUARDADOS: {cantidadFavs}
          </Link>

          {usuario ? (
            <>
              <span className="saludo-usuario">
                Hola, <strong>{usuario.nombreUsuario}</strong>
              </span>
              <button onClick={cerrarSesion} className="btn-sesion-neon">
                CERRAR SESION
              </button>
            </>
          ) : !loading ? (
            <Link to="/login" className="btn-sesion-neon">
              INICIAR SESION
            </Link>
          ) : null}
        </div>
      </header>

      <Offcanvas
        show={mostrarMenu}
        onHide={cerrarMenu}
        placement="start"
        className="offcanvas-juegotodo"
      >
        <Offcanvas.Header
          closeButton
          closeVariant="white"
          className="offcanvas-header-juegotodo"
        >
          <Offcanvas.Title className="offcanvas-title-juegotodo">
            JUEGOTODO
          </Offcanvas.Title>
        </Offcanvas.Header>

        <Offcanvas.Body className="d-flex flex-column gap-2 pt-4">
          <Link to="/" onClick={cerrarMenu} className="item-menu-lateral">INICIO</Link>
          <Link to="/products" onClick={cerrarMenu} className="item-menu-lateral">CATALOGO COMPLETO</Link>
          <Link to="/favoritos" onClick={cerrarMenu} className="item-menu-lateral">JUEGOS FAVORITOS ({cantidadFavs})</Link>
          <Link to="/ofertas" onClick={cerrarMenu} className="item-menu-lateral">OFERTAS</Link>
          <Link to="/contacto" onClick={cerrarMenu} className="item-menu-lateral">CONTACTO</Link>
          {isAdmin && <Link to="/admin" onClick={cerrarMenu} className="item-menu-lateral">ADMINISTRADOR</Link>}

          <hr className="offcanvas-divider" />

          <div className="offcanvas-subtitle">
            CATEGORIAS RAPIDAS
          </div>
          <Link to="/products?categoria=ACCION" onClick={cerrarMenu} className="item-menu-lateral sub">Accion</Link>
          <Link to="/products?categoria=DEPORTE" onClick={cerrarMenu} className="item-menu-lateral sub">Deporte</Link>
          <Link to="/products?categoria=HISTORIA" onClick={cerrarMenu} className="item-menu-lateral sub">Historia</Link>
          <Link to="/products?categoria=SHOOTERS" onClick={cerrarMenu} className="item-menu-lateral sub">Shooters</Link>
          <Link to="/products?categoria=MULTIJUGADOR" onClick={cerrarMenu} className="item-menu-lateral sub">Multijugador</Link>
        </Offcanvas.Body>
      </Offcanvas>
    </>
  );
}

export default NavBar;
