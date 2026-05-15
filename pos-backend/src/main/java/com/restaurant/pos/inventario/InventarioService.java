package com.restaurant.pos.inventario;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.common.PaginationParams;
import com.restaurant.pos.common.ValidationException;
import com.restaurant.pos.pedido.ItemPedido;
import com.restaurant.pos.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventarioService {

    private static final Logger LOG = Logger.getLogger(InventarioService.class);

    // -------------------------------------------------------
    // CRUD
    // -------------------------------------------------------

    public List<ItemInventarioDTO> findAll(PaginationParams pagination) {
        return ItemInventario.findAll(pagination.getPage(), pagination.getSize())
                .stream()
                .map(item -> {
                    BigDecimal reservado = ReservaInventario.sumReservasByItemInventarioId(item.id);
                    return ItemInventarioDTO.from(item, reservado);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemInventarioDTO create(CreateItemInventarioRequest request) {
        ItemInventario item = new ItemInventario();
        item.nombre = request.nombre;
        item.unidadMedida = request.unidadMedida;
        item.stockActual = request.stockActual != null ? request.stockActual : BigDecimal.ZERO;
        item.stockMinimo = request.stockMinimo != null ? request.stockMinimo : BigDecimal.ZERO;
        item.stockMaximo = request.stockMaximo;

        if (request.categoriaId != null) {
            item.categoria = com.restaurant.pos.producto.Categoria.findByIdOptional(request.categoriaId)
                    .map(c -> (com.restaurant.pos.producto.Categoria) c)
                    .orElse(null);
        }

        item.persist();

        // Si hay stock inicial, registra movimiento ENTRADA
        if (item.stockActual.compareTo(BigDecimal.ZERO) > 0) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.itemInventario = item;
            mov.tipo = TipoMovimiento.ENTRADA;
            mov.cantidad = item.stockActual;
            mov.motivo = "Stock inicial";
            mov.fechaHora = java.time.LocalDateTime.now();
            mov.persist();
        }

        LOG.infof("ItemInventario created: nombre=%s stockActual=%s", item.nombre, item.stockActual);
        return ItemInventarioDTO.from(item, BigDecimal.ZERO);
    }

    @Transactional
    public ItemInventarioDTO update(UUID id, CreateItemInventarioRequest request) {
        ItemInventario item = findItemOrThrow(id);
        if (request.nombre != null) item.nombre = request.nombre;
        if (request.unidadMedida != null) item.unidadMedida = request.unidadMedida;
        if (request.stockMinimo != null) item.stockMinimo = request.stockMinimo;
        if (request.stockMaximo != null) item.stockMaximo = request.stockMaximo;
        BigDecimal reservado = ReservaInventario.sumReservasByItemInventarioId(id);
        return ItemInventarioDTO.from(item, reservado);
    }

    // -------------------------------------------------------
    // Movements
    // -------------------------------------------------------

    @Transactional
    public MovimientoInventarioDTO registrarMovimiento(UUID id,
                                                        RegistrarMovimientoRequest request,
                                                        UUID userId) {
        ItemInventario item = findItemOrThrow(id);

        // AJUSTE and MERMA require motivo
        if ((request.tipo == TipoMovimiento.AJUSTE || request.tipo == TipoMovimiento.MERMA)
                && (request.motivo == null || request.motivo.isBlank())) {
            throw new ValidationException("motivo", "El motivo es obligatorio para ajustes y mermas");
        }

        BigDecimal nuevoCantidad;
        if (request.tipo == TipoMovimiento.ENTRADA) {
            nuevoCantidad = item.stockActual.add(request.cantidad);
        } else {
            // AJUSTE, MERMA: subtract
            nuevoCantidad = item.stockActual.subtract(request.cantidad);
            if (nuevoCantidad.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400,
                        "La operación resultaría en stock negativo. Stock actual: " + item.stockActual);
            }
        }

        item.stockActual = nuevoCantidad;

        MovimientoInventario mov = new MovimientoInventario();
        mov.itemInventario = item;
        mov.usuario = userId != null ? (Usuario) Usuario.findById(userId).orElse(null) : null;
        mov.tipo = request.tipo;
        mov.cantidad = request.cantidad;
        mov.motivo = request.motivo;
        mov.proveedor = request.proveedor;
        mov.fechaHora = LocalDateTime.now();
        mov.persist();

        // Alert if below minimum
        if (item.stockActual.compareTo(item.stockMinimo) < 0) {
            LOG.warnf("Stock alert: item=%s stockActual=%s stockMinimo=%s",
                    item.nombre, item.stockActual, item.stockMinimo);
        }

        LOG.infof("Movimiento registered: item=%s tipo=%s cantidad=%s",
                item.nombre, request.tipo, request.cantidad);
        return MovimientoInventarioDTO.from(mov);
    }

    public List<MovimientoInventarioDTO> getMovimientos(UUID id,
                                                         LocalDateTime desde,
                                                         LocalDateTime hasta,
                                                         PaginationParams pagination) {
        findItemOrThrow(id);
        LocalDateTime from = desde != null ? desde : LocalDateTime.now().minusMonths(1);
        LocalDateTime to = hasta != null ? hasta : LocalDateTime.now();
        return MovimientoInventario.findByItemAndPeriod(id, from, to,
                        pagination.getPage(), pagination.getSize())
                .stream()
                .map(MovimientoInventarioDTO::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Reservations (called by PedidoService)
    // -------------------------------------------------------

    /**
     * Reserves stock for an ItemPedido.
     * Throws StockInsuficienteException if available stock < cantidad.
     */
    @Transactional
    public void reservar(UUID itemInventarioId, BigDecimal cantidad, UUID itemPedidoId) {
        ItemInventario item = findItemOrThrow(itemInventarioId);

        BigDecimal reservado = ReservaInventario.sumReservasByItemInventarioId(itemInventarioId);
        BigDecimal disponible = item.stockActual.subtract(reservado);

        if (disponible.compareTo(cantidad) < 0) {
            throw new BusinessException.StockInsuficienteException(
                    "Stock insuficiente para '" + item.nombre +
                    "'. Disponible: " + disponible + ", solicitado: " + cantidad);
        }

        ReservaInventario reserva = new ReservaInventario();
        reserva.itemInventario = item;
        reserva.itemPedido = (ItemPedido) ItemPedido.findByIdOptional(itemPedidoId).orElse(null);
        reserva.cantidad = cantidad;
        reserva.persist();

        LOG.infof("Stock reserved: item=%s cantidad=%s itemPedidoId=%s",
                item.nombre, cantidad, itemPedidoId);
    }

    /**
     * Releases the reservation for an ItemPedido (item removed before sending to kitchen).
     */
    @Transactional
    public void liberarReserva(UUID itemPedidoId) {
        List<ReservaInventario> reservas = ReservaInventario.findByItemPedidoId(itemPedidoId);
        reservas.forEach(r -> {
            LOG.infof("Releasing reservation: item=%s cantidad=%s",
                    r.itemInventario.nombre, r.cantidad);
            r.delete();
        });
    }

    /**
     * Converts reservations to SALIDA_VENTA movements (called on cobro).
     */
    @Transactional
    public void confirmarReserva(UUID itemPedidoId, UUID userId) {
        List<ReservaInventario> reservas = ReservaInventario.findByItemPedidoId(itemPedidoId);
        for (ReservaInventario reserva : reservas) {
            ItemInventario item = reserva.itemInventario;

            // Deduct from actual stock
            item.stockActual = item.stockActual.subtract(reserva.cantidad);
            if (item.stockActual.compareTo(BigDecimal.ZERO) < 0) {
                item.stockActual = BigDecimal.ZERO;
            }

            // Create movement record
            MovimientoInventario mov = new MovimientoInventario();
            mov.itemInventario = item;
            mov.usuario = userId != null ? (Usuario) Usuario.findById(userId).orElse(null) : null;
            mov.tipo = TipoMovimiento.SALIDA_VENTA;
            mov.cantidad = reserva.cantidad;
            mov.motivo = "Venta confirmada - ItemPedido: " + itemPedidoId;
            mov.fechaHora = LocalDateTime.now();
            mov.persist();

            reserva.delete();
        }
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private ItemInventario findItemOrThrow(UUID id) {
        return ItemInventario.findByIdOptional(id)
                .map(i -> (ItemInventario) i)
                .orElseThrow(() -> new BusinessException(404, "Item de inventario no encontrado: " + id));
    }
}
