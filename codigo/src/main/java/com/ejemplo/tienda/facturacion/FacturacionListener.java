package com.ejemplo.tienda.facturacion;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import com.ejemplo.tienda.pedidos.internal.OrderController;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.ejemplo.tienda.pedidos.internal.OrderController;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FacturacionListener {
    private OrderController violacionTemporal;

    private final AtomicInteger consecutivo = new AtomicInteger(0);
    private OrderController violacionTemporal;
    @EventListener
    @Order(1)
    public void onPedidoConfirmado(PedidoConfirmado evento) {
        if ("C-999".equals(evento.clienteId())) {
            System.out.printf("FACTURA FALLO pedido=%d cliente=%s%n",
                    evento.pedidoId(), evento.clienteId());
            return;
        }

        int numero = consecutivo.incrementAndGet();
        String codigoFactura = String.format("F-%04d", numero);
        System.out.printf("FACTURA %s pedido=%d cliente=%s total=%d%n",
                codigoFactura, evento.pedidoId(), evento.clienteId(), evento.total());
    }
}