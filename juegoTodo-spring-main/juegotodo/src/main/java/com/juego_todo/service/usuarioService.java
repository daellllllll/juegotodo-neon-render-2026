package com.juego_todo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juego_todo.dto.AuthSession;
import com.juego_todo.dto.authResponseDTO;
import com.juego_todo.dto.loginRequestDTO;
import com.juego_todo.dto.registroRequestDTO;
import com.juego_todo.dto.usuarioDTO;
import com.juego_todo.model.refreshToken;
import com.juego_todo.model.usuario;
import com.juego_todo.repository.refreshTokenRepository;
import com.juego_todo.repository.usuarioRepository;
import com.juego_todo.security.JwtUtil;

import io.jsonwebtoken.Claims;

@Service
@Transactional
public class usuarioService {

    private final usuarioRepository usuarioRepository;
    private final refreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public usuarioService(
            usuarioRepository usuarioRepository,
            refreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public List<usuarioDTO> obtenerTodo() {
        return usuarioRepository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public usuarioDTO buscarPorId(Long id) {
        return convertirADTO(usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe")));
    }

    @Transactional(readOnly = true)
    public usuarioDTO buscarPorCorreo(String correo) {
        return convertirADTO(usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe")));
    }

    public AuthSession registrarUsuario(registroRequestDTO dto) {
        String correo = normalizarCorreo(dto.getCorreo());
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        usuario user = new usuario();
        user.setNombreUsuario(dto.getNombreUsuario().trim());
        user.setCorreo(correo);
        user.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        user.setRol("USER");

        return crearSesion(usuarioRepository.save(user));
    }

    public AuthSession login(loginRequestDTO credenciales) {
        String correo = normalizarCorreo(credenciales.getCorreo());
        usuario user = usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(u -> passwordEncoder.matches(credenciales.getContrasena(), u.getContrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        return crearSesion(user);
    }

    public AuthSession renovarToken(String refreshJwt) {
        if (refreshJwt == null || !jwtUtil.validarRefreshToken(refreshJwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión venció. Inicia sesión nuevamente.");
        }

        Claims claims = jwtUtil.extraerClaims(refreshJwt);
        Number userIdClaim = claims.get("idUsuario", Number.class);
        if (userIdClaim == null || claims.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de renovación inválido");
        }

        refreshToken sesion = refreshTokenRepository.findByJtiHashAndRevocadoFalse(hash(claims.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión ya no está activa"));
        if (!sesion.getExpiraEn().isAfter(LocalDateTime.now())) {
            sesion.setRevocado(true);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión venció. Inicia sesión nuevamente.");
        }

        usuario user = usuarioRepository.findById(userIdClaim.longValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no está disponible"));
        sesion.setRevocado(true);
        return crearSesion(user);
    }

    public void cerrarSesion(String refreshJwt) {
        if (refreshJwt == null || !jwtUtil.validarRefreshToken(refreshJwt)) {
            return;
        }

        String jti = jwtUtil.extraerClaims(refreshJwt).getId();
        refreshTokenRepository.findByJtiHashAndRevocadoFalse(hash(jti))
                .ifPresent(token -> token.setRevocado(true));
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe");
        }
        refreshTokenRepository.deleteAllByUsuarioIdUsuario(id);
        usuarioRepository.deleteById(id);
    }

    private AuthSession crearSesion(usuario user) {
        String accessToken = jwtUtil.generarAccessToken(user.getIdUsuario(), user.getCorreo(), user.getRol());
        String jti = UUID.randomUUID().toString();
        String refreshJwt = jwtUtil.generarRefreshToken(user.getIdUsuario(), user.getCorreo(), user.getRol(), jti);

        refreshToken sesion = new refreshToken();
        sesion.setUsuario(user);
        sesion.setJtiHash(hash(jti));
        sesion.setExpiraEn(LocalDateTime.now().plusNanos(jwtUtil.obtenerDuracionRefresh() * 1_000_000));
        sesion.setRevocado(false);
        refreshTokenRepository.save(sesion);

        authResponseDTO respuesta = new authResponseDTO();
        respuesta.setToken(accessToken);
        respuesta.setIdUsuario(user.getIdUsuario());
        respuesta.setNombreUsuario(user.getNombreUsuario());
        respuesta.setCorreo(user.getCorreo());
        respuesta.setRol(user.getRol());

        return new AuthSession(respuesta, refreshJwt);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private usuarioDTO convertirADTO(usuario user) {
        usuarioDTO dto = new usuarioDTO();
        dto.setIdUsuario(user.getIdUsuario());
        dto.setNombreUsuario(user.getNombreUsuario());
        dto.setCorreo(user.getCorreo());
        dto.setRol(user.getRol());
        return dto;
    }
}
