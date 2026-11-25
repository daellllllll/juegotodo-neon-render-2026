# Juegotodo backend

API REST para el catálogo de videojuegos. Usa Spring Boot, PostgreSQL, JPA, JWT, roles `USER` y `ADMIN`, y OpenAPI/Swagger.

## Ejecutar en local

Requisitos: JDK 21 y PowerShell, macOS/Linux o una terminal compatible con Maven Wrapper.

1. Copia `.env.example` a `.env` y reemplaza las variables. Usa una base de datos de desarrollo.
2. Define `JWT_SECRET` con un valor aleatorio de al menos 32 bytes. No uses el ejemplo como secreto real.
3. Desde esta carpeta ejecuta `./mvnw spring-boot:run` (Windows PowerShell: `./mvnw.cmd spring-boot:run`).
4. El backend usa `http://localhost:8080`. Swagger está en `/swagger-ui.html` y OpenAPI en `/v3/api-docs`.

La base se configura con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. El proyecto crea o actualiza las tablas y carga las 55 fichas del catálogo inicial si la tabla de productos está vacía. Las credenciales solo pertenecen al backend y no deben subirse a GitHub.

## Crear el primer administrador

El registro público siempre asigna `USER`; el frontend no puede elegir un rol. Después de registrar la cuenta que administrará el catálogo, un responsable debe promoverla en Neon SQL Editor:

```sql
UPDATE usuarios
SET rol = 'ADMIN'
WHERE correo = lower('correo-del-administrador@ejemplo.cl');
```

Luego cierra e inicia sesión para recibir un JWT con el rol actualizado. Nunca uses una contraseña administrativa fija en el frontend.

## Despliegue en Render y Neon

Selecciona como raíz de Render la carpeta que contiene este `pom.xml` y `Dockerfile`, y crea un servicio Docker. El Dockerfile usa Java 21, igual que Maven.

Configura en Render `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (único y aleatorio), `CORS_ORIGINS` (origen exacto del frontend, sin `/` al final), `AUTH_REFRESH_COOKIE_SECURE=true` y `AUTH_REFRESH_COOKIE_SAME_SITE=None`. Render entrega `PORT` automáticamente. Mantén las claves en Environment, nunca en el repositorio.

Para desarrollo local, `CORS_ORIGINS=http://localhost:5173` y la cookie `Lax`/no segura son los valores por defecto. Para una publicación web entre dominios, la cookie debe usar `SameSite=None` y `Secure`.

## Autenticación

Login y registro devuelven un JWT de acceso de 15 minutos y guardan el refresh JWT en una cookie `HttpOnly`. El backend guarda el hash del identificador de la sesión, rota el refresh token y permite revocarlo al cerrar sesión. El frontend conserva el token de acceso en memoria y renueva la sesión después de recargar.

Las lecturas de catálogo son públicas. Crear, editar o borrar productos y categorías requiere `ADMIN`. Favoritos requiere autenticación y siempre se asocia a la cuenta del token. El usuario registrado no puede consultar ni modificar los favoritos de otra cuenta.
