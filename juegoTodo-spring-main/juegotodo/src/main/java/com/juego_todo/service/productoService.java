package com.juego_todo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juego_todo.dto.productoDTO;
import com.juego_todo.model.categoria;
import com.juego_todo.model.producto;
import com.juego_todo.repository.categoriaRepository;
import com.juego_todo.repository.productoRepository;

@Service
@Transactional
public class productoService {

    private final productoRepository productoRepository;
    private final categoriaRepository categoriaRepository;

    public productoService(productoRepository productoRepository, categoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<productoDTO> obtenerTodo() {
        return productoRepository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public productoDTO buscarPorId(Long id) {
        return convertirADTO(productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe")));
    }

    @Transactional(readOnly = true)
    public List<productoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre.trim()).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<productoDTO> buscarPorCategoria(Long idCategoria) {
        if (!categoriaRepository.existsById(idCategoria)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe");
        }
        return productoRepository.findByCategoriaIdCategoria(idCategoria).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<productoDTO> obtenerOfertas() {
        return productoRepository.findByOfertaTrue().stream().map(this::convertirADTO).toList();
    }

    public productoDTO guardarProducto(productoDTO dto) {
        producto prod = new producto();
        aplicarDatos(prod, dto);
        return convertirADTO(productoRepository.save(prod));
    }

    public productoDTO actualizarProducto(Long id, productoDTO dto) {
        producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));
        aplicarDatos(existente, dto);
        return convertirADTO(productoRepository.save(existente));
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe");
        }
        productoRepository.deleteById(id);
    }

    private void aplicarDatos(producto prod, productoDTO dto) {
        prod.setNombre(dto.getNombre().trim());
        prod.setPrecio(dto.getPrecio());
        prod.setDescripcion(dto.getDescripcion());
        prod.setOferta(Boolean.TRUE.equals(dto.getOferta()));
        prod.setImagenUrl(dto.getImagenUrl());
        prod.setLinkTiendaOficial(dto.getLinkTiendaOficial());

        if (dto.getIdCategoria() == null) {
            prod.setCategoria(null);
        } else {
            categoria cat = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe"));
            prod.setCategoria(cat);
        }
    }

    private productoDTO convertirADTO(producto prod) {
        productoDTO dto = new productoDTO();
        dto.setIdProducto(prod.getIdProducto());
        dto.setNombre(prod.getNombre());
        dto.setPrecio(prod.getPrecio());
        dto.setDescripcion(prod.getDescripcion());
        dto.setOferta(prod.getOferta());
        dto.setImagenUrl(prod.getImagenUrl());
        dto.setLinkTiendaOficial(prod.getLinkTiendaOficial());
        if (prod.getCategoria() != null) {
            dto.setIdCategoria(prod.getCategoria().getIdCategoria());
            dto.setNombreCategoria(prod.getCategoria().getNombreCategoria());
        }
        return dto;
    }
}
