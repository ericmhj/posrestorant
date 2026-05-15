package com.restaurant.pos.inventario;

import com.restaurant.pos.producto.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "item_inventario")
public class ItemInventario extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "nombre", length = 150, nullable = false)
    public String nombre;

    @Column(name = "unidad_medida", length = 30)
    public String unidadMedida;

    @Column(name = "stock_actual", precision = 10, scale = 3, nullable = false)
    public BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", precision = 10, scale = 3, nullable = false)
    public BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_maximo", precision = 10, scale = 3)
    public BigDecimal stockMaximo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    public Categoria categoria;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    /**
     * Returns items where stock_actual is below stock_minimo.
     */
    public static List<ItemInventario> findBelowMinStock() {
        return list("stockActual < stockMinimo ORDER BY nombre ASC");
    }

    /**
     * Returns a page of all inventory items ordered by nombre.
     */
    public static List<ItemInventario> findAll(int page, int size) {
        return findAll(io.quarkus.panache.common.Sort.by("nombre"))
                .page(page, size)
                .list();
    }
}
