package com.juego_todo.dto;

public record AuthSession(authResponseDTO respuesta, String refreshToken) {
}
