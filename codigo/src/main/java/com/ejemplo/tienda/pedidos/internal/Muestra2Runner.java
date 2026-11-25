package com.ejemplo.tienda.pedidos.internal;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Muestra de la Hoja 2 (Q2 / Fila 1): entra por el controlador desde arranque limpio. */
@Component
@Profile("muestra2")
class Muestra2Runner {

    private final OrderController controller;

    Muestra2Runner(OrderController controller) {
        this.controller = controller;
    }

    @EventListener(ApplicationReadyEvent.class)
    void correr() {
        pedir("C-001", 4);   // u - 1 -> PEDIDO 1, FACTURA F-0001, AVISO
        pedir("C-999", 4);   // u - 1 -> PEDIDO 2, FACTURA FALLO, AVISO
        pedir("C-002", 5);   // u     -> PEDIDO 3, FACTURA F-0002, AVISO
    }

    private void pedir(String cliente, int cantidad) {
        OrderResponse r = controller.create(new OrderRequest(cliente, 1L, cantidad)).getBody();
        System.out.println("PEDIDO " + r.id() + " " + r.estado()
                + " total=" + r.total().toPlainString());
    }
}