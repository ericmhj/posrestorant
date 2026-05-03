package com.restaurant.pos.producto;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "producto")
public class Producto extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    public Categoria categoria;

    @Column(name = "nombre", length = 150, nullable = false)
    public String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    public String descripcion;

    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    public BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estacion", length = 20, nullable = false)
    public Estacion estacion;

    @Column(name = "activo")
    public Boolean activo = true;

    @Column(name = "imagen_url", length = 500)
    public String imagenUrl;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    /**
     * Returns a page of active products ordered by nombre.
     */
    public static List<Producto> findActivos(int page, int size) {
        return find("activo = true ORDER BY nombre")
                .page(page, size)
                .list();
    }

    public static List<Producto> findByCategoriaId(UUID categoriaId) {
        return list("categoria.id = ?1 AND activo = true ORDER BY nombre", categoriaId);
    }

    public static Optional<Producto> findActivoById(UUID id) {
        return find("id = ?1 AND activo = true", id).firstResultOptional();
    }
}
