package com.juego_todo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juego_todo.model.categoria;
import com.juego_todo.model.producto;
import com.juego_todo.repository.categoriaRepository;
import com.juego_todo.repository.productoRepository;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final categoriaRepository categoriaRepository;
    private final productoRepository productoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatosIniciales(
            categoriaRepository categoriaRepository,
            productoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (productoRepository.count() > 0) {
            return;
        }

        List<ProductoInicial> catalogo;
        try (InputStream input = new ClassPathResource("productos-iniciales.json").getInputStream()) {
            catalogo = objectMapper.readValue(input, new TypeReference<>() {});
        }

        Map<String, categoria> categorias = new HashMap<>();
        for (categoria existente : categoriaRepository.findAll()) {
            categorias.put(existente.getNombreCategoria().toUpperCase(), existente);
        }

        for (ProductoInicial inicial : catalogo) {
            String nombreCategoria = inicial.categoria().trim().toUpperCase();
            categoria cat = categorias.computeIfAbsent(nombreCategoria, nombre -> {
                categoria nueva = new categoria();
                nueva.setNombreCategoria(nombre);
                nueva.setImagenUrl(null);
                return categoriaRepository.save(nueva);
            });

            producto prod = new producto();
            prod.setNombre(inicial.nombre().trim());
            prod.setCategoria(cat);
            prod.setPrecio(inicial.precio());
            prod.setDescripcion(inicial.descripcion());
            prod.setOferta(inicial.oferta());
            prod.setImagenUrl(inicial.imagenUrl());
            prod.setLinkTiendaOficial(inicial.linkTiendaOficial());
            productoRepository.save(prod);
        }
    }

    private record ProductoInicial(
            String nombre,
            String categoria,
            Double precio,
            String descripcion,
            Boolean oferta,
            String imagenUrl,
            String linkTiendaOficial) {
    }
}
