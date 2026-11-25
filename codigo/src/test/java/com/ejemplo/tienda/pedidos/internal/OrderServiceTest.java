package com.ejemplo.tienda.pedidos.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private CatalogApi catalog; // fake catalog: the real module is not needed

    @Mock
    private ApplicationEventPublisher events;

    @InjectMocks
    private OrderService service;

    @Test
    void registrarReservesStockAndKeepsASnapshotOfTheProduct() {
        when(catalog.getProduct(1L))
                .thenReturn(new ProductSummary(1L, "Mouse", new BigDecimal("45000")));
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = service.registrar("C-001", 1L, 2);

        verify(catalog).reserveStock(1L, 2);
        assertThat(order.getLines()).hasSize(1);
        assertThat(order.getLines().get(0).productName()).isEqualTo("Mouse");
        assertThat(order.getLines().get(0).quantity()).isEqualTo(2);
        assertThat(order.total()).isEqualByComparingTo("90000");
    }

    @Test
    void registrarSavesARejectedOrderWhenTheCatalogRefusesTheStock() {
        when(catalog.getProduct(1L))
                .thenReturn(new ProductSummary(1L, "Mouse", new BigDecimal("45000")));
        when(catalog.reserveStock(1L, 9))
                .thenThrow(new InsufficientStockException(1L, 9, 2));
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = service.registrar("C-001", 1L, 9);

        assertThat(order.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        assertThat(order.getTotalPesos()).isZero();
        verify(repository).save(any(Order.class));
    }

    @Test
    void findByIdThrowsWhenTheOrderDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }
}