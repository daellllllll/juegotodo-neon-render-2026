package com.juego_todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juego_todo.model.categoria;

public interface categoriaRepository extends JpaRepository<categoria, Long> {

}
