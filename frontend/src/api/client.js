const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

export async function apiRequest(path, options = {}) {
  const { token, headers: suppliedHeaders, ...fetchOptions } = options;
  const headers = new Headers(suppliedHeaders || {});

  if (fetchOptions.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...fetchOptions,
      headers,
      credentials: 'include',
    });
  } catch {
    throw new ApiError('No se pudo conectar con el servidor. Revisa la URL de la API.', 0, null);
  }

  if (response.status === 204) return null;

  const contentType = response.headers.get('content-type') || '';
  const body = contentType.includes('application/json') ? await response.json() : null;
  if (!response.ok) {
    const message = body?.mensaje || body?.message || `La solicitud falló (${response.status}).`;
    throw new ApiError(message, response.status, body);
  }
  return body;
}

export { API_URL };
