import React, { useState } from 'react';
import { Container } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import AuthForm from '../components/organisms/AuthForm';
import '../styles/login.css';

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, registrar } = useAuth();
  const [esRegistro, setEsRegistro] = useState(false);
  const [formulario, setFormulario] = useState({
    usuario: '',
    email: '',
    password: ''
  });
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });

  const handleChange = (e) => {
    setFormulario({
      ...formulario,
      [e.target.name]: e.target.value
    });
  };

  const handleToggleModo = () => {
    setEsRegistro(!esRegistro);
    setMensaje({ texto: '', tipo: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje({ texto: '', tipo: '' });
    try {
      const usuario = esRegistro
        ? await registrar({
            nombreUsuario: formulario.usuario.trim(),
            correo: formulario.email.trim(),
            contrasena: formulario.password,
          })
        : await login(formulario.email.trim(), formulario.password);
      setMensaje({ texto: `Sesión iniciada para ${usuario.nombreUsuario}.`, tipo: 'exito' });
      window.setTimeout(() => {
        navigate(location.state?.from?.pathname || '/', { replace: true });
      }, 400);
    } catch (errorApi) {
      setMensaje({ texto: errorApi.message, tipo: 'error' });
    }
  };

  return (
    <Container className="my-5">
      <AuthForm
        esRegistro={esRegistro}
        formulario={formulario}
        mensaje={mensaje}
        onChange={handleChange}
        onSubmit={handleSubmit}
        onToggleModo={handleToggleModo}
      />
    </Container>
  );
}

export default Login;
