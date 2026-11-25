package com.ejemplo.tienda.pedidos.internal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotBlank(message = "El cliente es obligatorio") String clienteId,
        @NotNull(message = "El producto es obligatorio") Long productId,
        @Min(value = 1, message = "La cantidad debe ser al menos 1") int quantity
) {
}
