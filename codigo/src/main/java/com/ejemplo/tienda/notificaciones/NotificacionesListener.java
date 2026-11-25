package com.ejemplo.tienda.notificaciones;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class NotificacionesListener {

    @EventListener
    @Order(2)
    public void onPedidoConfirmado(PedidoConfirmado evento) {
        System.out.printf("AVISO cliente=%s%n", evento.clienteId());
    }
}