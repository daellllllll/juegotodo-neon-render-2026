package com.juego_todo.dto;

import lombok.Data;

@Data
public class authResponseDTO {
    private String token;
    private Long idUsuario;
    private String nombreUsuario;
    private String correo;
    private String rol;
}