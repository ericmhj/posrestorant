package com.restaurant.pos.producto;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categoria")
public class Categoria extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "nombre", length = 100, nullable = false)
    public String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    public String descripcion;

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    public static List<Categoria> findAllOrdered() {
        return listAll(Sort.by("nombre"));
    }

    /**
     * Counts active and inactive products belonging to the given category.
     */
    public static long countProductosByCategoriaId(UUID categoriaId) {
        return Producto.count("categoria.id", categoriaId);
    }
}
