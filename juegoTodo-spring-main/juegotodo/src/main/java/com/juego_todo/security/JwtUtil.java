package com.juego_todo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarAccessToken(Long idUsuario, String correo, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiration);

        return Jwts.builder()
                .subject(correo)
                .claim("idUsuario", idUsuario)
                .claim("rol", rol.toUpperCase())
                .claim("tipo", "access")
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public String generarRefreshToken(Long idUsuario, String correo, String rol, String jti) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(correo)
                .id(jti == null ? UUID.randomUUID().toString() : jti)
                .claim("idUsuario", idUsuario)
                .claim("rol", rol.toUpperCase())
                .claim("tipo", "refresh")
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean validarToken(String token) {
        try {
            Claims claims = extraerClaims(token);
            return "access".equals(claims.get("tipo", String.class))
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validarRefreshToken(String token) {
        try {
            Claims claims = extraerClaims(token);
            return "refresh".equals(claims.get("tipo", String.class))
                    && claims.getId() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long obtenerDuracionRefresh() {
        return refreshExpiration;
    }
}
