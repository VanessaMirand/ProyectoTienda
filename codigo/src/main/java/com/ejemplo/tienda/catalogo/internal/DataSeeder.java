package com.ejemplo.tienda.catalogo.internal;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Fila 1: un único producto, Mouse, con productoId 1. */
@Component
class DataSeeder implements CommandLineRunner {

    private final ProductRepository repository;

    DataSeeder(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // ya hay datos: no duplicar
        }
        repository.save(new Product("Mouse", "Mouse del ejercicio (fila 1)",
                new BigDecimal("45000"), 40, "electronica"));
    }
}