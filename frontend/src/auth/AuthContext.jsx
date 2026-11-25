import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '../api/client';

const AuthContext = createContext(null);
let refreshPromise = null;

function renovarSesion() {
  if (!refreshPromise) {
    refreshPromise = apiRequest('/api/usuarios/renovar', { method: 'POST' })
      .finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

function usuarioDesdeRespuesta(respuesta) {
  return {
    idUsuario: respuesta.idUsuario,
    nombreUsuario: respuesta.nombreUsuario,
    correo: respuesta.correo,
    rol: respuesta.rol,
  };
}

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [usuario, setUsuario] = useState(null);
  const [loading, setLoading] = useState(true);

  const guardarSesion = useCallback((respuesta) => {
    setAccessToken(respuesta.token);
    setUsuario(usuarioDesdeRespuesta(respuesta));
  }, []);

  const limpiarSesion = useCallback(() => {
    setAccessToken(null);
    setUsuario(null);
  }, []);

  useEffect(() => {
    let activo = true;
    renovarSesion()
      .then((respuesta) => {
        if (activo) guardarSesion(respuesta);
      })
      .catch(() => {
        if (activo) limpiarSesion();
      })
      .finally(() => {
        if (activo) setLoading(false);
      });
    return () => {
      activo = false;
    };
  }, [guardarSesion, limpiarSesion]);

  const request = useCallback(async (path, options = {}) => {
    const tokenUsado = options.token ?? accessToken;
    try {
      return await apiRequest(path, { ...options, token: tokenUsado });
    } catch (error) {
      if (error.status !== 401 || !tokenUsado || path === '/api/usuarios/renovar') {
        throw error;
      }

      try {
        const sesionRenovada = await renovarSesion();
        guardarSesion(sesionRenovada);
        return await apiRequest(path, { ...options, token: sesionRenovada.token });
      } catch (errorRenovacion) {
        limpiarSesion();
        throw errorRenovacion;
      }
    }
  }, [accessToken, guardarSesion, limpiarSesion]);

  const login = useCallback(async (correo, contrasena) => {
    const respuesta = await apiRequest('/api/usuarios/login', {
      method: 'POST',
      body: JSON.stringify({ correo, contrasena }),
    });
    guardarSesion(respuesta);
    return usuarioDesdeRespuesta(respuesta);
  }, [guardarSesion]);

  const registrar = useCallback(async ({ nombreUsuario, correo, contrasena }) => {
    const respuesta = await apiRequest('/api/usuarios/registro', {
      method: 'POST',
      body: JSON.stringify({ nombreUsuario, correo, contrasena }),
    });
    guardarSesion(respuesta);
    return usuarioDesdeRespuesta(respuesta);
  }, [guardarSesion]);

  const logout = useCallback(async () => {
    try {
      await apiRequest('/api/usuarios/logout', { method: 'POST', token: accessToken });
    } finally {
      limpiarSesion();
    }
  }, [accessToken, limpiarSesion]);

  const value = useMemo(() => ({
    accessToken,
    usuario,
    loading,
    isAdmin: usuario?.rol === 'ADMIN',
    request,
    login,
    registrar,
    logout,
  }), [accessToken, usuario, loading, request, login, registrar, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return context;
}
