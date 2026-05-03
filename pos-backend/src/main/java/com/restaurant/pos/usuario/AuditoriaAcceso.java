package com.restaurant.pos.usuario;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "auditoria_acceso")
public class AuditoriaAcceso extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @Column(name = "accion", length = 50)
    public String accion;

    @Column(name = "resultado", length = 20)
    public String resultado;

    @Column(name = "fecha_hora")
    public LocalDateTime fechaHora;

    @Column(name = "ip_address", length = 45)
    public String ipAddress;

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
     * Returns the most recent {@code limit} audit entries for the given user,
     * ordered by fecha_hora descending.
     */
    public static List<AuditoriaAcceso> findByUsuarioId(UUID usuarioId, int limit) {
        return find("usuario.id = ?1", Sort.by("fechaHora", Sort.Direction.Descending), usuarioId)
                .page(0, limit)
                .list();
    }
}
