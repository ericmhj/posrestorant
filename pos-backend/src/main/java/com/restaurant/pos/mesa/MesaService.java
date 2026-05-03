package com.restaurant.pos.mesa;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
import com.restaurant.pos.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MesaService {

    private static final Logger LOG = Logger.getLogger(MesaService.class);

    public List<MesaDTO> findAll() {
        return Mesa.findAllOrdered().stream().map(m -> {
            MesaDTO dto = MesaDTO.from(m);
            // Enrich with open cuenta info if occupied
            if (m.estado == MesaEstado.OCUPADA) {
                Cuenta.findAbiertaByMesaId(m.id).ifPresent(c -> {
                    dto.cuentaId = c.id;
                    dto.totalAcumulado = c.total != null ? c.total.doubleValue() : 0.0;
                    if (c.abiertaEn != null) {
                        dto.tiempoAbierta = Duration.between(c.abiertaEn, LocalDateTime.now()).getSeconds();
                    }
                });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public MesaDTO create(CreateMesaRequest request) {
        Mesa mesa = new Mesa();
        mesa.nombre = request.nombre;
        mesa.estado = MesaEstado.LIBRE;
        mesa.persist();
        LOG.infof("Mesa created: nombre=%s", mesa.nombre);
        return MesaDTO.from(mesa);
    }

    @Transactional
    public MesaDTO update(UUID id, CreateMesaRequest request) {
        Mesa mesa = Mesa.findByIdOptional(id)
                .map(m -> (Mesa) m)
                .orElseThrow(() -> new BusinessException(404, "Mesa no encontrada: " + id));
        mesa.nombre = request.nombre;
        mesa.updatedAt = LocalDateTime.now();
        return MesaDTO.from(mesa);
    }

    @Transactional
    public void delete(UUID id) {
        Mesa mesa = Mesa.findByIdOptional(id)
                .map(m -> (Mesa) m)
                .orElseThrow(() -> new BusinessException(404, "Mesa no encontrada: " + id));

        if (Cuenta.findAbiertaByMesaId(id).isPresent()) {
            throw new BusinessException(409, "No se puede eliminar la mesa porque tiene una cuenta abierta");
        }
        mesa.delete();
        LOG.infof("Mesa deleted: id=%s nombre=%s", id, mesa.nombre);
    }

    @Transactional
    public Cuenta abrir(UUID mesaId, UUID meseroId) {
        Mesa mesa = Mesa.findByIdOptional(mesaId)
                .map(m -> (Mesa) m)
                .orElseThrow(() -> new BusinessException(404, "Mesa no encontrada: " + mesaId));

        if (mesa.estado != MesaEstado.LIBRE) {
            throw new BusinessException(409, "La mesa ya tiene una cuenta abierta");
        }

        mesa.estado = MesaEstado.OCUPADA;
        mesa.updatedAt = LocalDateTime.now();

        Cuenta cuenta = new Cuenta();
        cuenta.mesa = mesa;
        cuenta.mesero = Usuario.findById(meseroId).orElse(null);
        cuenta.estado = CuentaEstado.ABIERTA;
        cuenta.abiertaEn = LocalDateTime.now();
        cuenta.total = java.math.BigDecimal.ZERO;
        cuenta.persist();

        LOG.infof("Mesa opened: mesaId=%s cuentaId=%s", mesaId, cuenta.id);
        return cuenta;
    }
}
