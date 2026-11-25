package com.juego_todo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juego_todo.model.favorito;

public interface favoritoRepository extends JpaRepository<favorito, Long> {
    List<favorito> findByUsuarioIdUsuario(Long idUsuario);
    Optional<favorito> findByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);
    boolean existsByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);
    void deleteByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);
}
