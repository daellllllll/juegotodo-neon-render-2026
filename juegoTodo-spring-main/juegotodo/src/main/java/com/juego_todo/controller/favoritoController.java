package com.juego_todo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juego_todo.dto.favoritoDTO;
import com.juego_todo.dto.favoritoRequestDTO;
import com.juego_todo.service.favoritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favoritos")
@SecurityRequirement(name = "bearerAuth")
public class favoritoController {

    private final favoritoService favoritoService;

    public favoritoController(favoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    @Operation(summary = "Lista los favoritos de la sesión actual.")
    public ResponseEntity<List<favoritoDTO>> obtenerMisFavoritos(Authentication authentication) {
        return ResponseEntity.ok(favoritoService.obtenerPorCorreo(authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Agrega un producto a los favoritos de la sesión actual.")
    public ResponseEntity<favoritoDTO> agregarFavorito(
            @Valid @RequestBody favoritoRequestDTO solicitud,
            Authentication authentication) {
        favoritoDTO guardado = favoritoService.agregar(authentication.getName(), solicitud.getIdProducto());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @DeleteMapping("/producto/{idProducto}")
    @Operation(summary = "Quita un producto de los favoritos de la sesión actual.")
    public ResponseEntity<Void> eliminarFavorito(
            @PathVariable Long idProducto,
            Authentication authentication) {
        favoritoService.eliminar(authentication.getName(), idProducto);
        return ResponseEntity.noContent().build();
    }
}
