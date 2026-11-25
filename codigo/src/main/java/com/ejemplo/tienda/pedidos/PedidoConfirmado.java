package com.ejemplo.tienda.pedidos;

/** Evento público del módulo pedidos: un pedido quedó CONFIRMADO. */
public record PedidoConfirmado(Long pedidoId, String clienteId, long total) {
}