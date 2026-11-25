package com.juego_todo.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data 
public class favoritoDTO {

    private Long idFavorito;
    private Long idUsuario;
    private Long idProducto;
    private LocalDateTime fechaAgregado;
    private productoDTO producto;
}
