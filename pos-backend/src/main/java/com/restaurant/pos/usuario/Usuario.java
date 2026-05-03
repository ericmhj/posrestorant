package com.restaurant.pos.usuario;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "nombre", length = 100, nullable = false)
    public String nombre;

    @Column(name = "apellido", length = 100, nullable = false)
    public String apellido;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    public String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20, nullable = false)
    public Rol rol;

    @Column(name = "activo")
    public Boolean activo = true;

    @Column(name = "intentos_fallidos")
    public Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    public LocalDateTime bloqueadoHasta;

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

    public static Optional<Usuario> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public static List<Usuario> findActivos() {
        return list("activo", true);
    }

    public static Optional<Usuario> findById(UUID id) {
        return findByIdOptional(id);
    }
}
