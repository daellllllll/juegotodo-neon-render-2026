package com.juego_todo.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juego_todo.dto.favoritoDTO;
import com.juego_todo.dto.productoDTO;
import com.juego_todo.model.favorito;
import com.juego_todo.model.producto;
import com.juego_todo.model.usuario;
import com.juego_todo.repository.favoritoRepository;
import com.juego_todo.repository.productoRepository;
import com.juego_todo.repository.usuarioRepository;

@Service
@Transactional
public class favoritoService {

    private final favoritoRepository favoritoRepository;
    private final usuarioRepository usuarioRepository;
    private final productoRepository productoRepository;

    public favoritoService(
            favoritoRepository favoritoRepository,
            usuarioRepository usuarioRepository,
            productoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<favoritoDTO> obtenerPorCorreo(String correo) {
        usuario user = buscarUsuario(correo);
        return favoritoRepository.findByUsuarioIdUsuario(user.getIdUsuario()).stream()
                .map(this::convertirADTO)
                .toList();
    }

    public favoritoDTO agregar(String correo, Long idProducto) {
        usuario user = buscarUsuario(correo);
        producto prod = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));

        if (favoritoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(user.getIdUsuario(), idProducto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya está en favoritos");
        }

        favorito favorito = new favorito();
        favorito.setUsuario(user);
        favorito.setProducto(prod);
        return convertirADTO(favoritoRepository.save(favorito));
    }

    public void eliminar(String correo, Long idProducto) {
        usuario user = buscarUsuario(correo);
        if (!favoritoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(user.getIdUsuario(), idProducto)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El favorito no existe");
        }
        favoritoRepository.deleteByUsuarioIdUsuarioAndProductoIdProducto(user.getIdUsuario(), idProducto);
    }

    private usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no está disponible"));
    }

    private favoritoDTO convertirADTO(favorito favorito) {
        favoritoDTO dto = new favoritoDTO();
        dto.setIdFavorito(favorito.getIdFavorito());
        dto.setIdUsuario(favorito.getUsuario().getIdUsuario());
        dto.setIdProducto(favorito.getProducto().getIdProducto());
        dto.setFechaAgregado(favorito.getFechaAgregado());
        dto.setProducto(convertirProductoADTO(favorito.getProducto()));
        return dto;
    }

    private productoDTO convertirProductoADTO(producto prod) {
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
