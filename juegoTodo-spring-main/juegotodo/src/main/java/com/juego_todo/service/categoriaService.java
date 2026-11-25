package com.juego_todo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juego_todo.dto.categoriaDTO;
import com.juego_todo.model.categoria;
import com.juego_todo.repository.categoriaRepository;

@Service
@Transactional
public class categoriaService {

    private final categoriaRepository categoriaRepository;

    public categoriaService(categoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<categoriaDTO> obtenerTodo() {
        return categoriaRepository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public categoriaDTO buscarPorId(Long id) {
        return convertirADTO(categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe")));
    }

    public categoriaDTO guardarCategoria(categoriaDTO dto) {
        categoria categoria = new categoria();
        categoria.setNombreCategoria(dto.getNombreCategoria().trim());
        categoria.setImagenUrl(dto.getImagenUrl());
        return convertirADTO(categoriaRepository.save(categoria));
    }

    public categoriaDTO actualizarCategoria(Long id, categoriaDTO dto) {
        categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe"));
        existente.setNombreCategoria(dto.getNombreCategoria().trim());
        existente.setImagenUrl(dto.getImagenUrl());
        return convertirADTO(categoriaRepository.save(existente));
    }

    public void eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe");
        }
        categoriaRepository.deleteById(id);
    }

    private categoriaDTO convertirADTO(categoria categoria) {
        categoriaDTO dto = new categoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombreCategoria(categoria.getNombreCategoria());
        dto.setImagenUrl(categoria.getImagenUrl());
        return dto;
    }
}
