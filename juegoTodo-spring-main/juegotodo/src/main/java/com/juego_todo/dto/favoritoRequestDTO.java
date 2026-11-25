package com.juego_todo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class favoritoRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El producto debe tener un identificador válido")
    private Long idProducto;
}
