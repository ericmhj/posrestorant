package com.restaurant.pos.pedido;

import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido")
public class Pedido extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id")
    public Cuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesero_id")
    public Usuario mesero;

    @Column(name = "numero_ronda", nullable = false)
    public Integer numeroRonda;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemPedido> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------
    // Static finders
    // -------------------------------------------------------

    public static List<Pedido> findByCuentaId(UUID cuentaId) {
        return list("cuenta.id = ?1 ORDER BY numeroRonda ASC", cuentaId);
    }
}
