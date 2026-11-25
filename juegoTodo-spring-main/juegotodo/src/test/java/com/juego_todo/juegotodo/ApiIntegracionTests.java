package com.juego_todo.juegotodo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juego_todo.model.categoria;
import com.juego_todo.model.producto;
import com.juego_todo.model.usuario;
import com.juego_todo.repository.categoriaRepository;
import com.juego_todo.repository.favoritoRepository;
import com.juego_todo.repository.productoRepository;
import com.juego_todo.repository.refreshTokenRepository;
import com.juego_todo.repository.usuarioRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private categoriaRepository categoriaRepository;

    @Autowired
    private productoRepository productoRepository;

    @Autowired
    private usuarioRepository usuarioRepository;

    @Autowired
    private favoritoRepository favoritoRepository;

    @Autowired
    private refreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idCategoria;
    private Long idProducto;

    @BeforeEach
    void prepararCatalogo() {
        refreshTokenRepository.deleteAll();
        favoritoRepository.deleteAll();
        usuarioRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        categoria categoria = new categoria();
        categoria.setNombreCategoria("HISTORIA");
        idCategoria = categoriaRepository.save(categoria).getIdCategoria();

        producto producto = new producto();
        producto.setCategoria(categoriaRepository.findById(idCategoria).orElseThrow());
        producto.setNombre("Juego de integración");
        producto.setPrecio(19990.0);
        producto.setDescripcion("Producto de prueba de integración");
        producto.setOferta(false);
        producto = productoRepository.save(producto);
        idProducto = producto.getIdProducto();
    }

    @Test
    void registroNoPermiteElegirRolYLasRutasDeEscrituraRequierenAdmin() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", "Usuario",
                                "correo", "usuario@example.cl",
                                "contrasena", "clave-segura-123",
                                "rol", "ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USER"))
                .andReturn();

        String token = objectMapper.readTree(registro.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Juego nuevo")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrearProductoYLaApiDevuelveLocation() throws Exception {
        usuario admin = crearUsuario("admin@example.cl", "ADMIN");
        String token = iniciarSesion(admin.getCorreo());

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Juego creado")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juego creado"))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotNull(
                        result.getResponse().getHeader("Location")));
    }

    @Test
    void refreshCookieSeRotaYNoPuedeReutilizarse() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", "Ana",
                                "correo", "ana@example.cl",
                                "contrasena", "clave-segura-123"))))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie refreshAnterior = registro.getResponse().getCookie("JT_REFRESH");
        org.junit.jupiter.api.Assertions.assertNotNull(refreshAnterior);

        MvcResult renovacion = mockMvc.perform(post("/api/usuarios/renovar").cookie(refreshAnterior))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refreshNuevo = renovacion.getResponse().getCookie("JT_REFRESH");
        org.junit.jupiter.api.Assertions.assertNotNull(refreshNuevo);
        org.junit.jupiter.api.Assertions.assertNotEquals(refreshAnterior.getValue(), refreshNuevo.getValue());

        mockMvc.perform(post("/api/usuarios/renovar").cookie(refreshAnterior))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioSoloVeSusFavoritos() throws Exception {
        String tokenAna = registrarYObtenerToken("ana@example.cl", "Ana");
        mockMvc.perform(post("/api/favoritos")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idProducto", idProducto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(idProducto.intValue()));

        String tokenBea = registrarYObtenerToken("bea@example.cl", "Bea");
        mockMvc.perform(get("/api/favoritos").header("Authorization", "Bearer " + tokenBea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private String registrarYObtenerToken(String correo, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", nombre,
                                "correo", correo,
                                "contrasena", "clave-segura-123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private usuario crearUsuario(String correo, String rol) {
        usuario usuario = new usuario();
        usuario.setNombreUsuario("Administrador");
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode("password"));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    private String iniciarSesion(String correo) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo,
                                "contrasena", "password"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private String productoJson(String nombre) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "idCategoria", idCategoria,
                "nombre", nombre,
                "precio", 24990,
                "descripcion", "Juego de ejemplo",
                "oferta", false,
                "imagenUrl", "https://example.cl/juego.jpg",
                "linkTiendaOficial", "https://example.cl/tienda"));
    }
}
