package com.juego_todo.controller;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.juego_todo.dto.AuthSession;
import com.juego_todo.dto.authResponseDTO;
import com.juego_todo.dto.loginRequestDTO;
import com.juego_todo.dto.registroRequestDTO;
import com.juego_todo.dto.usuarioDTO;
import com.juego_todo.service.usuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class usuarioController {

    private static final String REFRESH_COOKIE_NAME = "JT_REFRESH";

    private final usuarioService usuarioService;

    @Value("${auth.refresh-cookie-secure}")
    private boolean refreshCookieSecure;

    @Value("${auth.refresh-cookie-same-site}")
    private String refreshCookieSameSite;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public usuarioController(usuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista usuarios. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<usuarioDTO>> todosLosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodo());
    }

    @GetMapping("/me")
    @Operation(summary = "Obtiene el perfil de la sesión actual.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<usuarioDTO> miPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.buscarPorCorreo(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario. ADMIN o el propio usuario.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<usuarioDTO> buscarPorId(@PathVariable Long id, Authentication authentication) {
        usuarioDTO usuario = usuarioService.buscarPorId(id);
        boolean esAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!esAdmin && !usuario.getCorreo().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar otra cuenta");
        }
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registra una cuenta con rol USER e inicia sesión.")
    public ResponseEntity<authResponseDTO> registrarUsuario(@Valid @RequestBody registroRequestDTO dto) {
        return respuestaConCookie(usuarioService.registrarUsuario(dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un JWT de acceso.")
    public ResponseEntity<authResponseDTO> login(@Valid @RequestBody loginRequestDTO credenciales) {
        return respuestaConCookie(usuarioService.login(credenciales), HttpStatus.OK);
    }

    @PostMapping("/renovar")
    @Operation(summary = "Rota el refresh token HttpOnly y entrega un nuevo JWT de acceso.")
    public ResponseEntity<authResponseDTO> renovarToken(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            jakarta.servlet.http.HttpServletRequest request) {
        validarOrigenCookie(request);
        return respuestaConCookie(usuarioService.renovarToken(refreshToken), HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoca la sesión actual y elimina la cookie de renovación.")
    public ResponseEntity<Void> cerrarSesion(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            jakarta.servlet.http.HttpServletRequest request) {
        validarOrigenCookie(request);
        usuarioService.cerrarSesion(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, crearCookie("", Duration.ZERO).toString())
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<authResponseDTO> respuestaConCookie(AuthSession session, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE,
                        crearCookie(session.refreshToken(), Duration.ofMillis(refreshExpiration)).toString())
                .body(session.respuesta());
    }

    private ResponseCookie crearCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/usuarios")
                .maxAge(maxAge)
                .build();
    }

    private void validarOrigenCookie(jakarta.servlet.http.HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            return;
        }

        boolean permitido = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .anyMatch(origin::equals);
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El origen no puede renovar esta sesión");
        }
    }
}
