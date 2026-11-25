package com.ejemplo.tienda.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.ProductSummary;
import com.ejemplo.tienda.pedidos.internal.EstadoPedido;
import com.ejemplo.tienda.pedidos.internal.Order;
import com.ejemplo.tienda.pedidos.internal.OrderService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Starts ONLY the orders module (with its real database) and replaces the
 * catalog with a mock. The catalog's beans are never created.
 */
@ApplicationModuleTest
class OrderModuleTests {

    @Autowired
    OrderService service;

    @Autowired
    ApplicationContext context;

    @MockitoBean
    CatalogApi catalog;

    @Test
    void createsAndStoresAnOrderUsingTheCatalogApi() {
        when(catalog.getProduct(1L))
                .thenReturn(new ProductSummary(1L, "Mouse", new BigDecimal("45000")));

        Order created = service.registrar("C-001", 1L, 2);

        Order stored = service.findById(created.getId());
        assertThat(stored.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(stored.total()).isEqualByComparingTo("90000");
        assertThat(stored.getLines().get(0).productName()).isEqualTo("Mouse");
    }

    @Test
    void theCatalogModuleIsNotPartOfThisTest() {
        assertThat(context.containsBean("productController")).isFalse();
        assertThat(context.containsBean("orderController")).isTrue();
    }
}