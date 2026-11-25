package com.ejemplo.tienda.pedidos.internal;

/** RN-1 y RN-2. Fila 1 (Mouse): umbral u = 5, descuento d = 10 %. */
final class CalculadoraTotal {

    static final int UMBRAL = 5;
    static final int DESCUENTO = 10;

    private CalculadoraTotal() {
    }

    static long total(long precio, int cantidad) {
        int descuento = cantidad >= UMBRAL ? DESCUENTO : 0;
        return cantidad * precio * (100 - descuento) / 100;
    }
}