function normalizarProducto(producto) {
  return {
    id: producto.idProducto,
    idCategoria: producto.idCategoria,
    name: producto.nombre,
    title: producto.nombre,
    nombre: producto.nombre,
    category: producto.nombreCategoria || '',
    categoria: producto.nombreCategoria || '',
    price: Number(producto.precio ?? 0),
    precio: Number(producto.precio ?? 0),
    description: producto.descripcion || '',
    descripcion: producto.descripcion || '',
    oferta: Boolean(producto.oferta),
    image: producto.imagenUrl || '',
    imagen: producto.imagenUrl || '',
    link: producto.linkTiendaOficial || '',
    linkTiendaOficial: producto.linkTiendaOficial || '',
  };
}

function productoParaApi(producto) {
  return {
    idCategoria: producto.idCategoria ?? producto.categoryId ?? null,
    nombre: (producto.nombre ?? producto.name ?? producto.title ?? '').trim(),
    precio: Number(producto.precio ?? producto.price ?? 0),
    descripcion: producto.descripcion ?? producto.description ?? '',
    oferta: Boolean(producto.oferta),
    imagenUrl: producto.imagenUrl ?? producto.imagen ?? producto.image ?? '',
    linkTiendaOficial: producto.linkTiendaOficial ?? producto.link ?? '',
  };
}

export async function getProducts(request) {
  const productos = await request('/api/productos');
  return (productos || []).map(normalizarProducto);
}

export async function getProductById(id, request) {
  const producto = await request(`/api/productos/${id}`);
  return normalizarProducto(producto);
}

export async function getCategories(request) {
  const categorias = await request('/api/categorias');
  return (categorias || []).map((categoria) => ({
    id: categoria.idCategoria,
    nombre: categoria.nombreCategoria,
    imagenUrl: categoria.imagenUrl || '',
  }));
}

export async function createProduct(producto, request) {
  const creado = await request('/api/productos', {
    method: 'POST',
    body: JSON.stringify(productoParaApi(producto)),
  });
  return normalizarProducto(creado);
}

export async function updateProduct(id, producto, request) {
  const actualizado = await request(`/api/productos/${id}`, {
    method: 'PUT',
    body: JSON.stringify(productoParaApi(producto)),
  });
  return normalizarProducto(actualizado);
}

export function deleteProduct(id, request) {
  return request(`/api/productos/${id}`, { method: 'DELETE' });
}

export async function getFavoriteProducts(request) {
  const favoritos = await request('/api/favoritos');
  return (favoritos || []).map((favorito) => ({
    ...normalizarProducto(favorito.producto),
    idFavorito: favorito.idFavorito,
  }));
}

export async function addFavorite(idProducto, request) {
  const favorito = await request('/api/favoritos', {
    method: 'POST',
    body: JSON.stringify({ idProducto }),
  });
  return normalizarProducto(favorito.producto);
}

export function removeFavorite(idProducto, request) {
  return request(`/api/favoritos/producto/${idProducto}`, { method: 'DELETE' });
}
