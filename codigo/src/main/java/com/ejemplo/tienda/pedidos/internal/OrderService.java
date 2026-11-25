package com.ejemplo.tienda.pedidos.internal;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository repository;
    private final CatalogApi catalog;
    private final ApplicationEventPublisher events;

    public OrderService(OrderRepository repository, CatalogApi catalog,
                        ApplicationEventPublisher events) {
        this.repository = repository;
        this.catalog = catalog;
        this.events = events;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Order findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /** RN-4: total comprado por un cliente (suma de sus pedidos CONFIRMADO). */
    public long totalComprado(String clienteId) {
        return repository.totalPorCliente(clienteId, EstadoPedido.CONFIRMADO);
    }

    // Sin transacción propia: si la reserva falla, el pedido RECHAZADO igual se
    // guarda (cada paso usa su transacción corta) y el id solo se consume al guardar.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Order registrar(String clienteId, Long productId, int quantity) {
        // RN-5: error de validación; no se registra ni consume id
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        ProductSummary product = catalog.getProduct(productId); // ProductNotFoundException

        Order order = new Order(clienteId);
        order.addLine(new OrderLine(product.id(), product.name(), product.price(), quantity));
        try {
            catalog.reserveStock(productId, quantity); // RN-3
            order.confirmar(CalculadoraTotal.total(product.price().longValueExact(), quantity));
        } catch (InsufficientStockException e) {
            order.rechazar();
        }
        Order saved = repository.save(order);
        if (saved.getEstado() == EstadoPedido.CONFIRMADO) {
            events.publishEvent(new PedidoConfirmado(
                    saved.getId(), saved.getClienteId(), saved.getTotalPesos()));
        }
        return saved;
    }
}