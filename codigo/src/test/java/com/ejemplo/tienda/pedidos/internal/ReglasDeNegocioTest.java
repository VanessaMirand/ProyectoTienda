package com.ejemplo.tienda.pedidos.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class ReglasDeNegocioTest {

    OrderRepository repo = mock(OrderRepository.class);
    CatalogApi catalog = mock(CatalogApi.class);
    ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    OrderService service = new OrderService(repo, catalog, events);

    @BeforeEach
    void preparar() {
        when(catalog.getProduct(1L))
                .thenReturn(new ProductSummary(1L, "Mouse", new BigDecimal("45000.00")));
        when(repo.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test // RN-1 y RN-2: justo por debajo del umbral, sin descuento
    void sinDescuentoPorDebajoDelUmbral() {
        assertThat(service.registrar("C-001", 1L, 4).getTotalPesos()).isEqualTo(180000);
    }

    @Test // RN-1: el umbral mismo ya cuenta (>=)
    void conDescuentoEnElUmbral() {
        assertThat(service.registrar("C-002", 1L, 5).getTotalPesos()).isEqualTo(202500);
    }

    @Test // RN-3: sin stock queda RECHAZADO, total 0 y no emite evento
    void rechazaCuandoNoHayStock() {
        when(catalog.reserveStock(1L, 32))
                .thenThrow(new InsufficientStockException(1L, 32, 31));
        Order o = service.registrar("C-001", 1L, 32);
        assertThat(o.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        assertThat(o.getTotalPesos()).isZero();
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test // un pedido confirmado publica el evento
    void confirmadoPublicaEvento() {
        service.registrar("C-001", 1L, 4);
        verify(events).publishEvent(any(PedidoConfirmado.class));
    }

    @Test // RN-5: cantidad <= 0 es error de validación y no se guarda
    void cantidadInvalidaNoSeRegistra() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar("C-001", 1L, 0));
        verify(repo, never()).save(any());
    }
}