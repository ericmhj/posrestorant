package com.restaurant.pos.producto;

import com.restaurant.pos.inventario.ItemInventario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "producto_ingrediente")
public class ProductoIngrediente extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_inventario_id", nullable = false)
    public ItemInventario itemInventario;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    public BigDecimal cantidad;

    public static List<ProductoIngrediente> findByProductoId(UUID productoId) {
        return list("producto.id", productoId);
    }
}
