package com.ejemplo.tienda.pedidos.internal;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Muestra de la Hoja 1: entra por el controlador, la entrada real. */
@Component
@Profile("muestra")
class MuestraRunner {

    private final OrderController controller;

    MuestraRunner(OrderController controller) {
        this.controller = controller;
    }

    @EventListener(ApplicationReadyEvent.class)
    void correr() {
        pedir("C-001", 4);   // u - 1
        pedir("C-002", 5);   // u
        pedir("C-001", 32);  // stock - 2u + 2
        pedir("C-003", 31);  // stock - 2u + 1
    }

    private void pedir(String cliente, int cantidad) {
        OrderResponse r = controller.create(new OrderRequest(cliente, 1L, cantidad)).getBody();
        System.out.println("PEDIDO " + r.id() + " " + r.estado()
                + " total=" + r.total().toPlainString());
    }
}
