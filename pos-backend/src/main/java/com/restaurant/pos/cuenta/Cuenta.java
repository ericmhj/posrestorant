package com.restaurant.pos.cuenta;

import com.restaurant.pos.mesa.Mesa;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "cuenta")
public class Cuenta extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    public Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesero_id")
    public Usuario mesero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    public CuentaEstado estado = CuentaEstado.ABIERTA;

    @Column(name = "abierta_en")
    public LocalDateTime abiertaEn;

    @Column(name = "cerrada_en")
    public LocalDateTime cerradaEn;

    @Column(name = "metodo_pago", length = 30)
    public String metodoPago;

    @Column(name = "total", precision = 10, scale = 2)
    public BigDecimal total;

    @PrePersist
    public void prePersist() {
        if (abiertaEn == null) {
            abiertaEn = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    /**
     * Returns the single open cuenta for the given mesa, if any.
     */
    public static Optional<Cuenta> findAbiertaByMesaId(UUID mesaId) {
        return find("mesa.id = ?1 AND estado = ?2", mesaId, CuentaEstado.ABIERTA)
                .firstResultOptional();
    }

    /**
     * Returns all cuentas (any state) for the given mesa, newest first.
     */
    public static List<Cuenta> findByMesaId(UUID mesaId) {
        return list("mesa.id = ?1 ORDER BY abiertaEn DESC", mesaId);
    }
}
