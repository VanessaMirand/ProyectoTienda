package com.ejemplo.tienda.pedidos.internal;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // GET /api/orders
    @GetMapping
    public List<OrderResponse> findAll() {
        return service.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    // GET /api/orders/1
    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return OrderResponse.from(service.findById(id));
    }

    // GET /api/orders/clientes/C-001/total  (RN-4)
    @GetMapping("/clientes/{clienteId}/total")
    public long totalComprado(@PathVariable String clienteId) {
        return service.totalComprado(clienteId);
    }

    // POST /api/orders -> 201 Created + Location header
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order created = service.registrar(
                request.clienteId(), request.productId(), request.quantity());
        OrderResponse response = OrderResponse.from(created);
        URI location = URI.create("/api/orders/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}