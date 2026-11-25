package com.ejemplo.tienda.pedidos.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select count(o) > 0 from Order o join o.lines l where l.productId = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    @Query("select coalesce(sum(o.totalPesos), 0) from Order o "
            + "where o.clienteId = :clienteId and o.estado = :estado")
    long totalPorCliente(@Param("clienteId") String clienteId,
                         @Param("estado") EstadoPedido estado);
}
