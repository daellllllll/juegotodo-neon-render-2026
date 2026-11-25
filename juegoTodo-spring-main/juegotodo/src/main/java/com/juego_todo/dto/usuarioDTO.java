package com.juego_todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data 
public class usuarioDTO {

    private Long idUsuario;
    private String nombreUsuario;
    private String correo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    private String rol;
}
