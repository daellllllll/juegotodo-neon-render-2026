# Juegotodo frontend

Frontend React y Vite para el catálogo de videojuegos. La aplicación consume el backend Spring Boot y no almacena cuentas, contraseñas, productos ni favoritos en el navegador.

## Ejecutar localmente

1. Instala Node.js y npm.
2. Copia `.env.example` a `.env`.
3. Deja `VITE_API_URL=http://localhost:8080` mientras el backend local esté en el puerto 8080.
4. Ejecuta `npm install` y luego `npm run dev`.
5. Abre la dirección local que indique Vite, normalmente `http://localhost:5173`.

Solo el backend debe tener variables `DB_*`. No pongas credenciales de Neon en este proyecto frontend. Vite publica al navegador las variables que comienzan con `VITE_`.

## Sesión y permisos

El JWT de acceso permanece en memoria mientras la página está abierta. La cookie HttpOnly de renovación permite recuperar la sesión después de recargar. El catálogo es público. Favoritos requieren una cuenta. El panel `/admin` requiere una sesión cuyo rol provenga del backend y sea `ADMIN`.

## Publicación en Vercel

Configura la carpeta que contiene este `package.json` como raíz del proyecto y define `VITE_API_URL` con la URL base del backend, sin una barra final. Configura además `CORS_ORIGINS` en el backend con el origen exacto de este frontend. En Render, para un frontend y backend en dominios distintos, configura `AUTH_REFRESH_COOKIE_SECURE=true` y `AUTH_REFRESH_COOKIE_SAME_SITE=None`.
