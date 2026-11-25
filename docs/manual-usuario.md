# Manual de usuario de Juegotodo

## Introducción

Juegotodo permite consultar un catálogo de videojuegos, revisar precios y ofertas, abrir el enlace de la tienda oficial y guardar favoritos en una cuenta personal. El manual está dirigido a visitantes, usuarios registrados y administradores del catálogo.

## Requisitos

- Un computador o teléfono con un navegador moderno y conexión a internet.
- Para administrar el catálogo, una cuenta habilitada con rol `ADMIN`.
- La aplicación web no requiere instalar un programa en el dispositivo.

## Acceso a Juegotodo

Abre la dirección web que entregue el equipo. En el inicio se muestran novedades y ofertas destacadas. La barra superior muestra el acceso a favoritos, la cuenta activa y el cierre de sesión. El menú lateral lleva al catálogo, ofertas, contacto y, para administradores, al panel de administración.

**Captura 1. Inicio de Juegotodo.** Debe mostrar la portada y el menú principal.

## Consultar el catálogo

1. Abre **Catálogo completo** en el menú.
2. Usa el campo de búsqueda para filtrar juegos por nombre.
3. Selecciona una categoría para limitar los resultados.
4. Cambia el orden para ver precios o nombres.
5. Selecciona **Limpiar filtros** para volver al catálogo completo.
6. Abre una tarjeta para revisar la descripción, precio, categoría y enlace oficial.

**Captura 2. Catálogo con búsqueda y filtros.** Debe mostrar resultados cargados desde el backend.

**Captura 3. Detalle de un videojuego.** Debe mostrar sus datos y el botón para visitar la tienda oficial.

## Crear una cuenta e iniciar sesión

1. Selecciona **Iniciar sesión**.
2. Para registrarte, cambia a **Crear cuenta** y completa nombre, correo y contraseña de al menos ocho caracteres.
3. Envía el formulario. La cuenta nueva queda con rol `USER` y entra a Juegotodo.
4. En otro acceso, inicia sesión con el mismo correo y contraseña.
5. Al recargar la página, Juegotodo recupera la sesión mientras la cookie de renovación siga vigente.
6. Selecciona **Cerrar sesión** para revocar la sesión actual.

La contraseña no se guarda en el navegador. El backend la almacena como hash BCrypt. El navegador guarda el refresh token en una cookie HttpOnly y el token de acceso solo permanece en memoria.

**Captura 4. Registro de cuenta.** Debe mostrar los campos y el mensaje de cuenta creada.

**Captura 5. Inicio de sesión.** Debe mostrar el nombre de la cuenta activa en la barra superior.

## Guardar y quitar favoritos

Solo una persona con sesión iniciada puede guardar favoritos.

1. Abre el detalle de un juego.
2. Selecciona **Agregar a favoritos**.
3. Abre **Mis juegos favoritos** para ver la lista asociada a tu cuenta.
4. Selecciona **Quitar** para eliminar un juego de la lista.

Cada cuenta recibe únicamente sus propios favoritos. Si no hay sesión, el botón de favoritos lleva al inicio de sesión.

**Captura 6. Favoritos de una cuenta.** Debe mostrar juegos guardados y la acción para quitarlos.

## Revisar ofertas

Abre **Ofertas** para ver los videojuegos que el equipo administrador marcó como oferta. Selecciona una tarjeta para revisar su detalle y visitar la tienda oficial.

**Captura 7. Catálogo de ofertas.** Debe mostrar al menos una oferta cargada desde el backend.

## Administración del catálogo

El panel `/admin` está disponible solo para una cuenta con rol `ADMIN`. Las cuentas recién registradas son `USER`; un responsable debe promover la cuenta autorizada en la base de datos antes de usar el panel.

1. Inicia sesión con la cuenta administradora.
2. Abre **Administrador** desde el menú.
3. Selecciona **Agregar juego** para crear una ficha, o **Editar** en una fila existente.
4. Completa el nombre, categoría, precio, descripción e imagen. Agrega el enlace a la tienda oficial si está disponible y marca la opción de oferta cuando corresponda.
5. Guarda los cambios. Juegotodo vuelve a consultar el catálogo del servidor.
6. Selecciona **Eliminar** y confirma para borrar un producto.
7. Cierra sesión para salir del panel.

La API también valida el rol `ADMIN`. Modificar la interfaz desde el navegador no otorga permisos de administración.

**Captura 8. Panel administrativo.** Debe mostrar el catálogo, controles de edición y sesión ADMIN.

**Captura 9. Formulario de producto.** Debe mostrar los campos para crear o editar una ficha.

**Captura 10. Acceso denegado a un usuario USER.** Inicia sesión con una cuenta normal e intenta abrir `/admin`; debe verse el mensaje **Acceso denegado**.

## Problemas comunes

| Síntoma | Qué hacer |
| --- | --- |
| El catálogo no carga | Revisa la conexión a internet y vuelve a cargar la página. Si persiste, informa al equipo técnico. |
| No puedes iniciar sesión | Comprueba el correo y la contraseña. Si olvidaste la contraseña, solicita ayuda al equipo; la aplicación aún no incluye recuperación automática. |
| No puedes guardar favoritos | Inicia sesión y vuelve a intentarlo. |
| El panel administrativo no aparece | Inicia sesión con una cuenta que tenga rol `ADMIN`. |
| La sesión termina después de varios días | Inicia sesión otra vez; la cookie de renovación tiene una fecha de vencimiento. |

## Evidencia de la guía

Las capturas 1 a 10 deben obtenerse de la aplicación desplegada o ejecutada con el backend. No reemplaces las capturas por ilustraciones simuladas.
