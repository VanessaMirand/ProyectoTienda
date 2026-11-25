package com.ejemplo.tienda.pedidos.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, length = 40)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(nullable = false)
    private long totalPesos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(String clienteId) {
        this.clienteId = clienteId;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
    }

    public void confirmar(long total) {
        this.estado = EstadoPedido.CONFIRMADO;
        this.totalPesos = total;
    }

    public void rechazar() {
        this.estado = EstadoPedido.RECHAZADO;
        this.totalPesos = 0;
    }

    public BigDecimal total() {
        return BigDecimal.valueOf(totalPesos);
    }

    public long getTotalPesos() {
        return totalPesos;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getClienteId() {
        return clienteId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<OrderLine> getLines() {
        return List.copyOf(lines);
    }
}