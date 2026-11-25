package com.juego_todo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class productoDTO {
    
    private Long idProducto;
    private Long idCategoria;
    private String nombreCategoria;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 160, message = "El nombre no puede superar 160 caracteres")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double precio;

    @Size(max = 4000, message = "La descripción no puede superar 4000 caracteres")
    private String descripcion;
    private Boolean oferta;

    @Size(max = 2048, message = "La URL de imagen no puede superar 2048 caracteres")
    private String imagenUrl;

    @Size(max = 2048, message = "El enlace no puede superar 2048 caracteres")
    private String linkTiendaOficial;
}
