package com.juego_todo.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.juego_todo.dto.productoDTO;
import com.juego_todo.service.productoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class productoController {

    private final productoService productoService;

    public productoController(productoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Lista el catálogo público de videojuegos.")
    public ResponseEntity<List<productoDTO>> todosLosProductos() {
        return ResponseEntity.ok(productoService.obtenerTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por identificador.")
    public ResponseEntity<productoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca productos por nombre, ignorando mayúsculas y minúsculas.")
    public ResponseEntity<List<productoDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/categoria/{idCategoria}")
    @Operation(summary = "Lista los productos de una categoría.")
    public ResponseEntity<List<productoDTO>> buscarPorCategoria(@PathVariable Long idCategoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(idCategoria));
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Lista los productos marcados como oferta.")
    public ResponseEntity<List<productoDTO>> obtenerOfertas() {
        return ResponseEntity.ok(productoService.obtenerOfertas());
    }

    @PostMapping
    @Operation(summary = "Crea un producto. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<productoDTO> agregarProducto(@Valid @RequestBody productoDTO dto) {
        productoDTO guardado = productoService.guardarProducto(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getIdProducto())
                .toUri();
        return ResponseEntity.created(location).body(guardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza un producto. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<productoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody productoDTO dto) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).header(HttpHeaders.CACHE_CONTROL, "no-store").build();
    }
}
