package com.restaurant.pos.inventario;

import com.restaurant.pos.pedido.ItemPedido;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reserva_inventario")
public class ReservaInventario extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_inventario_id")
    public ItemInventario itemInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_pedido_id")
    public ItemPedido itemPedido;

    @Column(name = "cantidad", precision = 10, scale = 3, nullable = false)
    public BigDecimal cantidad;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    public static List<ReservaInventario> findByItemPedidoId(UUID itemPedidoId) {
        return list("itemPedido.id", itemPedidoId);
    }

    /**
     * Returns all active reservations for items belonging to the given cuenta.
     */
    public static List<ReservaInventario> findByCuentaId(UUID cuentaId) {
        return list("itemPedido.pedido.cuenta.id = ?1", cuentaId);
    }

    /**
     * Returns the total reserved quantity for the given inventory item across all active reservations.
     */
    public static BigDecimal sumReservasByItemInventarioId(UUID itemInventarioId) {
        Object result = getEntityManager()
                .createQuery("SELECT COALESCE(SUM(r.cantidad), 0) FROM ReservaInventario r WHERE r.itemInventario.id = ?1", BigDecimal.class)
                .setParameter(1, itemInventarioId)
                .getSingleResult();
        return result != null ? (BigDecimal) result : BigDecimal.ZERO;
    }
}
