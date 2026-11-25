import React, { useCallback, useEffect, useState } from 'react';
import { Container } from 'react-bootstrap';
import { useAuth } from '../auth/AuthContext';
import { createProduct, deleteProduct, getCategories, getProducts, updateProduct } from '../data/products';
import Text from '../components/atoms/Text';
import AdminProductTable from '../components/organisms/AdminProductTable';
import AdminProductModal from '../components/organisms/AdminProductModal';
import '../styles/admin.css';

const formularioVacio = (categoria = '') => ({
  nombre: '',
  categoria,
  precio: '',
  descripcion: '',
  imagen: '',
  linkTiendaOficial: '',
  oferta: false,
});

function Admin() {
  const { usuario, request, logout } = useAuth();
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);
  const [guardando, setGuardando] = useState(false);
  const [form, setForm] = useState(formularioVacio());

  const cargarCatalogo = useCallback(async () => {
    setCargando(true);
    try {
      const [productosApi, categoriasApi] = await Promise.all([
        getProducts(request),
        getCategories(request),
      ]);
      setProductos(productosApi);
      setCategorias(categoriasApi);
      setError('');
    } catch (errorApi) {
      setError(errorApi.message);
    } finally {
      setCargando(false);
    }
  }, [request]);

  useEffect(() => {
    cargarCatalogo();
  }, [cargarCatalogo]);

  const handleOpenModal = (producto = null) => {
    if (producto) {
      setEditId(producto.id);
      setForm({
        nombre: producto.nombre || '',
        categoria: producto.category || categorias[0]?.nombre || '',
        precio: producto.precio ?? '',
        descripcion: producto.descripcion || '',
        imagen: producto.imagen || '',
        linkTiendaOficial: producto.linkTiendaOficial || '',
        oferta: producto.oferta,
      });
    } else {
      setEditId(null);
      setForm(formularioVacio(categorias[0]?.nombre || ''));
    }
    setShowModal(true);
  };

  const handleSave = async (event) => {
    event.preventDefault();
    const categoria = categorias.find((item) => item.nombre === form.categoria);
    if (!categoria) {
      setError('Selecciona una categoría válida.');
      return;
    }

    setGuardando(true);
    try {
      const producto = {
        nombre: form.nombre,
        idCategoria: categoria.id,
        precio: Number(form.precio),
        descripcion: form.descripcion,
        imagenUrl: form.imagen,
        linkTiendaOficial: form.linkTiendaOficial,
        oferta: form.oferta,
      };
      if (editId) {
        await updateProduct(editId, producto, request);
      } else {
        await createProduct(producto, request);
      }
      setShowModal(false);
      setError('');
      await cargarCatalogo();
    } catch (errorApi) {
      setError(errorApi.message);
    } finally {
      setGuardando(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Seguro que deseas eliminar este juego del catálogo?')) return;
    try {
      await deleteProduct(id, request);
      await cargarCatalogo();
      setError('');
    } catch (errorApi) {
      setError(errorApi.message);
    }
  };

  const cerrarSesionAdmin = async () => {
    try {
      await logout();
    } catch {
      setError('La sesión se cerró en esta aplicación, pero no se pudo confirmar en el servidor.');
    }
  };

  return (
    <Container className="my-5">
      <div className="admin-header">
        <div>
          <Text variant="h2" className="admin-titulo">
            PANEL ADMINISTRATIVO
          </Text>
          <Text variant="span" className="admin-subtitulo-sesion">
            Sesión activa como {usuario?.nombreUsuario} (ADMIN)
          </Text>
        </div>

        <div className="d-flex gap-2">
          <button
            type="button"
            onClick={() => handleOpenModal()}
            className="btn-sesion-neon py-2 px-3"
            disabled={categorias.length === 0}
          >
            + AGREGAR JUEGO
          </button>
          <button
            type="button"
            onClick={cerrarSesionAdmin}
            className="btn-sesion-neon btn-salir-admin py-2 px-3"
          >
            SALIR
          </button>
        </div>
      </div>

      {error && <p role="alert" className="text-danger">{error}</p>}
      {categorias.length === 0 && !cargando && <p>No hay categorías disponibles para crear productos.</p>}
      {cargando ? (
        <p role="status">Cargando catálogo...</p>
      ) : (
        <AdminProductTable
          productos={productos}
          onEdit={handleOpenModal}
          onDelete={handleDelete}
        />
      )}

      <AdminProductModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSave}
        editId={editId}
        form={form}
        setForm={setForm}
        categorias={categorias}
        guardando={guardando}
      />
    </Container>
  );
}

export default Admin;
