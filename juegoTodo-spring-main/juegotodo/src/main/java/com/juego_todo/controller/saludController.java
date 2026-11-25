package com.juego_todo.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class saludController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("estado", "ok");
    }
}