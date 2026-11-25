# API e integración de Juegotodo

Este documento describe la API implementada en el backend de Juegotodo y cómo la consume el frontend React. El ejemplo de Pokémon de los anexos sirve como formato de documentación; las rutas reales corresponden al catálogo de videojuegos del proyecto.

## URL y documentación

- Desarrollo local: `http://localhost:8080`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Health check público: `GET /api/health`
- Frontend local: `http://localhost:5173`

El frontend define su URL mediante `VITE_API_URL`. El backend limita CORS a los orígenes declarados en `CORS_ORIGINS` y permite credenciales para el refresh cookie.

## Modelo de datos

| Entidad | Datos principales | Relación |
| --- | --- | --- |
| `categoria` | id, nombre, imagen | Una categoría agrupa videojuegos. |
| `producto` | id, categoría, nombre, precio, descripción, oferta, imagen y tienda oficial | Pertenece a una categoría. |
| `usuarios` | id, nombre, correo, contraseña BCrypt y rol | Puede guardar favoritos. |
| `favoritos` | id, usuario, producto y fecha | Cada favorito pertenece al usuario autenticado. |
| `refresh_tokens` | usuario, hash JTI, expiración y revocación | Permite rotar y cerrar sesiones. |

Si la tabla de productos parte vacía, Spring carga 55 videojuegos de ejemplo y sus cinco categorías iniciales.

## Endpoints

`USER` es el rol asignado al registrarse. `ADMIN` requiere una promoción hecha desde la base de datos. El frontend nunca envía un rol para obtener privilegios.

| Método | Ruta | Acceso | Respuesta principal |
| --- | --- | --- | --- |
| GET | `/api/health` | Público | `200` estado del servicio |
| GET | `/api/productos` | Público | `200` lista; `[]` si no hay registros |
| GET | `/api/productos/{id}` | Público | `200`, `404` si no existe |
| GET | `/api/productos/buscar?nombre=zelda` | Público | `200` lista filtrada |
| GET | `/api/productos/categoria/{idCategoria}` | Público | `200` lista filtrada, `404` si categoría no existe |
| GET | `/api/productos/ofertas` | Público | `200` lista de ofertas |
| POST | `/api/productos` | ADMIN | `201`, cuerpo creado y cabecera `Location` |
| PUT | `/api/productos/{id}` | ADMIN | `200`, producto actualizado |
| DELETE | `/api/productos/{id}` | ADMIN | `204`, sin cuerpo |
| GET | `/api/categorias` | Público | `200` lista de categorías |
| GET | `/api/categorias/{id}` | Público | `200`, `404` si no existe |
| POST | `/api/categorias` | ADMIN | `201` categoría creada |
| PUT | `/api/categorias/{id}` | ADMIN | `200` categoría actualizada |
| DELETE | `/api/categorias/{id}` | ADMIN | `204` |
| POST | `/api/usuarios/registro` | Público | `201` perfil y JWT de acceso; cookie HttpOnly |
| POST | `/api/usuarios/login` | Público | `200` perfil y JWT de acceso; cookie HttpOnly |
| POST | `/api/usuarios/renovar` | Cookie HttpOnly | `200` nuevo JWT; rota el refresh token |
| POST | `/api/usuarios/logout` | Cookie HttpOnly | `204`; revoca sesión y elimina cookie |
| GET | `/api/usuarios/me` | USER o ADMIN | `200` perfil de la sesión |
| GET | `/api/usuarios` | ADMIN | `200` lista de perfiles sin contraseñas |
| GET | `/api/usuarios/{id}` | Propio usuario o ADMIN | `200`, `403` para otra cuenta |
| DELETE | `/api/usuarios/{id}` | ADMIN | `204` |
| GET | `/api/favoritos` | USER o ADMIN | `200` favoritos de la sesión |
| POST | `/api/favoritos` | USER o ADMIN | `201` favorito creado |
| DELETE | `/api/favoritos/producto/{idProducto}` | USER o ADMIN | `204` favorito eliminado |

### Ejemplos de cuerpos

Registro:

```json
{
  "nombreUsuario": "Ana",
  "correo": "ana@ejemplo.cl",
  "contrasena": "una-clave-segura"
}
```

Login:

```json
{
  "correo": "ana@ejemplo.cl",
  "contrasena": "una-clave-segura"
}
```

Crear producto:

```json
{
  "idCategoria": 1,
  "nombre": "Juego de ejemplo",
  "precio": 19990,
  "descripcion": "Descripción del videojuego",
  "oferta": false,
  "imagenUrl": "https://ejemplo.cl/imagen.jpg",
  "linkTiendaOficial": "https://ejemplo.cl/tienda"
}
```

Agregar favorito:

```json
{ "idProducto": 1 }
```

Las listas vacías responden `200` y un arreglo vacío. El backend valida campos requeridos, correo, longitud de contraseña y precio no negativo.

## Errores

| Código | Significado |
| --- | --- |
| `400` | JSON o campos inválidos. Las validaciones incluyen un mapa `campos`. |
| `401` | No hay sesión válida o la cuenta/contraseña no coincide. |
| `403` | La sesión existe, pero el rol no permite la operación. |
| `404` | No se encontró el producto, usuario o categoría solicitado. |
| `409` | El correo ya está registrado, el producto ya es favorito o hay un conflicto de integridad. |
| `415` | El cuerpo debe enviarse como `application/json`. |
| `500` | Error interno; la respuesta no expone detalles de servidor. |

## Flujo entre frontend y backend

1. React obtiene productos y categorías con solicitudes `GET` públicas.
2. Registro o login envía correo y contraseña por HTTPS. Spring verifica la contraseña BCrypt y genera JWT de acceso.
3. El backend retorna el token de acceso en JSON y el refresh token como cookie `HttpOnly`, `Secure` en producción y `SameSite=None` cuando frontend/backend usan dominios distintos.
4. React mantiene el token de acceso en memoria y lo envía como `Authorization: Bearer <token>` en acciones protegidas. La cookie se envía con `credentials: include`.
5. Al recargar la página o vencer el JWT, React llama a `/api/usuarios/renovar`; el backend verifica el refresh JWT y su sesión guardada, revoca el token anterior y devuelve uno nuevo.
6. Al cerrar sesión, React solicita `/logout`; el backend revoca la sesión y vence la cookie.
7. El backend vuelve a comprobar roles y propiedad de favoritos en cada operación; ocultar botones en React no sustituye la autorización del servidor.

## Configuración por entorno

El backend requiere `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`. En Render se definen en Environment. `CORS_ORIGINS` debe contener el origen exacto del frontend, sin barra final. El frontend solo requiere `VITE_API_URL`; nunca debe contener credenciales de Neon.

## Pruebas de integración

`ApiIntegracionTests` usa una base H2 aislada y no necesita credenciales Neon. Cubre registro sin elevación de rol, autorización ADMIN para escribir productos, creación con cabecera `Location`, rotación e invalidación del refresh token y separación de favoritos por usuario. `JuegotodoApplicationTests` también usa el perfil `test`.

Para ejecutarlas desde la carpeta que contiene `pom.xml`: `./mvnw test` (PowerShell: `./mvnw.cmd test`). En esta entrega no ejecuté la suite.

## Evidencia visual

Las capturas de la interfaz deben tomarse desde la aplicación ejecutándose con el backend y datos de prueba. No se deben usar capturas simuladas como evidencia de una respuesta real.
