package com.juego_todo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.juego_todo.model.producto;

public interface productoRepository extends JpaRepository<producto, Long> {
    List<producto> findByCategoriaIdCategoria(Long idCategoria);
    List<producto> findByNombreContainingIgnoreCase(String nombre);
    List<producto> findByOfertaTrue();
}