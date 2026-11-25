package com.juego_todo.juegotodo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.juego_todo")
@EnableJpaRepositories(basePackages = "com.juego_todo.repository")
@EntityScan(basePackages = "com.juego_todo.model")
public class JuegotodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JuegotodoApplication.class, args);
	}

}