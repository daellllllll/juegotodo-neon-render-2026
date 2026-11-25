import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function ProtectedRoute({ role }) {
  const { usuario, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <p className="text-center py-5" role="status">Comprobando sesión...</p>;
  }
  if (!usuario) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  if (role && usuario.rol !== role) {
    return (
      <section className="text-center py-5" role="alert">
        <h2>Acceso denegado</h2>
        <p>Tu cuenta no tiene permiso para abrir esta sección.</p>
        <Link to="/">Volver al inicio</Link>
      </section>
    );
  }
  return <Outlet />;
}
