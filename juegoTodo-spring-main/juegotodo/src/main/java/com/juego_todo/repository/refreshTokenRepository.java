package com.juego_todo.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import com.juego_todo.model.refreshToken;

public interface refreshTokenRepository extends JpaRepository<refreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<refreshToken> findByJtiHashAndRevocadoFalse(String jtiHash);
    void deleteAllByUsuarioIdUsuario(Long idUsuario);
}
