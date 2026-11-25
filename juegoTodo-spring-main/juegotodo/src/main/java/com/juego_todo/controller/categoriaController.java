package com.juego_todo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juego_todo.dto.categoriaDTO;
import com.juego_todo.service.categoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class categoriaController {

    private final categoriaService categoriaService;

    public categoriaController(categoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista las categorías públicas del catálogo.")
    public ResponseEntity<List<categoriaDTO>> todasLasCategorias() {
        return ResponseEntity.ok(categoriaService.obtenerTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoría por identificador.")
    public ResponseEntity<categoriaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crea una categoría. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<categoriaDTO> agregarCategoria(@Valid @RequestBody categoriaDTO dto) {
        return ResponseEntity.status(201).body(categoriaService.guardarCategoria(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<categoriaDTO> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody categoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una categoría. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
