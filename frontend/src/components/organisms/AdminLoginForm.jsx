import React from 'react';
import Text from '../atoms/Text';
import Input from '../atoms/Input';
import '../../styles/admin.css';

function AdminLoginForm({ credenciales = {}, setCredenciales, onSubmit, errorLogin }) {
  const actualizar = (campo, valor) => {
    if (setCredenciales) setCredenciales((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <div className="admin-login-card">
      <Text variant="h2" className="text-center mb-4 admin-login-title">
        ACCESO ADMINISTRATIVO
      </Text>

      {errorLogin && <div className="admin-error-box" role="alert">{errorLogin}</div>}

      <form onSubmit={onSubmit} className="d-flex flex-column gap-3">
        <div>
          <Text variant="label" className="admin-label">Correo de administrador</Text>
          <Input
            type="email"
            placeholder="correo@ejemplo.com"
            value={credenciales.correo || ''}
            onChange={(event) => actualizar('correo', event.target.value)}
            required
          />
        </div>

        <div>
          <Text variant="label" className="admin-label">Contraseña</Text>
          <Input
            type="password"
            placeholder="Contraseña de la cuenta"
            value={credenciales.password || ''}
            onChange={(event) => actualizar('password', event.target.value)}
            required
          />
        </div>

        <button type="submit" className="btn-sesion-neon py-3 mt-2 admin-submit-btn">
          INGRESAR AL PANEL
        </button>
      </form>
    </div>
  );
}

export default AdminLoginForm;
