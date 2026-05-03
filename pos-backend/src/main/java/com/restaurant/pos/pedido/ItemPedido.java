package com.restaurant.pos.pedido;

import com.restaurant.pos.producto.Estacion;
import com.restaurant.pos.producto.Producto;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "item_pedido")
public class ItemPedido extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    public Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    public Producto producto;

    @Column(name = "cantidad", nullable = false)
    public Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    public BigDecimal precioUnitario;

    @Column(name = "modificadores", columnDefinition = "TEXT")
    public String modificadores;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    public ItemPedidoEstado estado = ItemPedidoEstado.PENDIENTE;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "preparando_en")
    public LocalDateTime preparandoEn;

    @Column(name = "listo_en")
    public LocalDateTime listoEn;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    /**
     * Returns items for the given estacion whose estado is in the provided list,
     * ordered by created_at ascending (oldest first for KDS/BDS display).
     */
    public static List<ItemPedido> findByEstacionAndEstados(Estacion estacion, List<ItemPedidoEstado> estados) {
        return find(
                "producto.estacion = ?1 AND estado IN ?2 ORDER BY createdAt ASC",
                estacion, estados
        ).list();
    }

    /**
     * Returns all items belonging to any pedido of the given cuenta.
     */
    public static List<ItemPedido> findByCuentaId(UUID cuentaId) {
        return find("pedido.cuenta.id = ?1 ORDER BY createdAt ASC", cuentaId).list();
    }

    /**
     * Returns PENDIENTE items for the given estacion, ordered by created_at ascending.
     */
    public static List<ItemPedido> findPendientesByEstacion(Estacion estacion) {
        return find(
                "producto.estacion = ?1 AND estado = ?2 ORDER BY createdAt ASC",
                estacion, ItemPedidoEstado.PENDIENTE
        ).list();
    }
}
