package com.juego_todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class categoriaDTO {

    private Long idCategoria;
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreCategoria;

    @Size(max = 2048, message = "La URL de imagen no puede superar 2048 caracteres")
    private String imagenUrl;
}
