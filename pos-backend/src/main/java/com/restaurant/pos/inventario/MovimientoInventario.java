package com.restaurant.pos.inventario;

import com.restaurant.pos.usuario.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_inventario_id")
    public ItemInventario itemInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30, nullable = false)
    public TipoMovimiento tipo;

    @Column(name = "cantidad", precision = 10, scale = 3, nullable = false)
    public BigDecimal cantidad;

    @Column(name = "motivo", columnDefinition = "TEXT")
    public String motivo;

    @Column(name = "proveedor", length = 150)
    public String proveedor;

    @Column(name = "fecha_hora")
    public LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    /**
     * Returns movements for the given item within the specified period,
     * ordered by fecha_hora descending, with pagination.
     */
    public static List<MovimientoInventario> findByItemAndPeriod(
            UUID itemInventarioId,
            LocalDateTime desde,
            LocalDateTime hasta,
            int page,
            int size) {
        return find(
                "itemInventario.id = ?1 AND fechaHora >= ?2 AND fechaHora <= ?3 ORDER BY fechaHora DESC",
                itemInventarioId, desde, hasta
        ).page(page, size).list();
    }
}
