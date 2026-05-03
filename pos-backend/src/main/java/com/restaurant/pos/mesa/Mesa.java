package com.restaurant.pos.mesa;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mesa")
public class Mesa extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "nombre", length = 50, nullable = false)
    public String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    public MesaEstado estado = MesaEstado.LIBRE;

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

    public static List<Mesa> findByEstado(MesaEstado estado) {
        return list("estado", estado);
    }

    public static List<Mesa> findAllOrdered() {
        return listAll(Sort.by("nombre"));
    }
}
