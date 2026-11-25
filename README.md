# Juegotodo

Proyecto full stack con frontend React, API Spring Boot, PostgreSQL en Neon, autenticación JWT y permisos por rol.

## Carpetas

- `frontend`: tienda React + Vite.
- `juegoTodo-spring-main/juegotodo`: API REST Spring Boot 4 y Java 21.
- `docs/api-integracion.md`: endpoints y flujo de autenticación.
- `docs/manual-usuario.md`: guía de uso.
- `render.yaml`: Blueprint de Render para desplegar la API y el frontend como servicios conectados.

## Despliegue en Neon y Render

1. Sube el repositorio a GitHub sin archivos `.env`.
2. En Render, crea un Blueprint desde ese repositorio y selecciona `render.yaml`.
3. En las variables que Render solicite para el backend, introduce `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` del proyecto Neon. No las agregues al repositorio ni a este archivo.
4. Render genera `JWT_SECRET` y conecta el frontend con la API. El Blueprint reserva las URL según los nombres de servicio incluidos en `render.yaml`.
5. La API espera en `/api/health`; Hibernate crea/actualiza las tablas y los datos iniciales al conectarse a Neon.

El Blueprint usa el plan gratuito para la API y un sitio estático para React. El backend puede dormir tras un periodo de inactividad; la primera petición puede demorar mientras inicia.

## Desarrollo local

1. En `juegoTodo-spring-main/juegotodo`, crea `.env` basándote en `.env.example` y completa una base PostgreSQL de desarrollo y un `JWT_SECRET` propio.
2. Inicia el backend desde esa carpeta con `./mvnw.cmd spring-boot:run` en PowerShell.
3. Desde `frontend`, crea `.env` con `VITE_API_URL=http://localhost:8080`, ejecuta `npm ci` y luego `npm run dev`.

No subas archivos `.env`. Si se compartió una contraseña de Neon fuera de un gestor de secretos, rótala antes de producción.
