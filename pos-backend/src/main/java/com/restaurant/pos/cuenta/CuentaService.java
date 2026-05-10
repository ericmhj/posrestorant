package com.restaurant.pos.cuenta;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.inventario.InventarioService;
import com.restaurant.pos.mesa.Mesa;
import com.restaurant.pos.mesa.MesaEstado;
import com.restaurant.pos.pedido.ItemPedido;
import com.restaurant.pos.pedido.ItemPedidoDTO;
import com.restaurant.pos.pedido.ItemPedidoEstado;
import com.restaurant.pos.pedido.Pedido;
import com.restaurant.pos.producto.Producto;
import com.restaurant.pos.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CuentaService {

    private static final Logger LOG = Logger.getLogger(CuentaService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.16"); // 16% IVA

    @Inject
    InventarioService inventarioService;

    // -------------------------------------------------------
    // Query
    // -------------------------------------------------------

    public CuentaDetalleDTO findById(UUID id) {
        Cuenta cuenta = findCuentaOrThrow(id);
        List<Pedido> pedidos = Pedido.findByCuentaId(id);
        return CuentaDetalleDTO.from(cuenta, pedidos);
    }

    // -------------------------------------------------------
    // Add pedido round
    // -------------------------------------------------------

    @Transactional
    public PedidoDTO addPedido(UUID cuentaId, CreatePedidoRequest request, UUID meseroId) {
        Cuenta cuenta = findCuentaOrThrow(cuentaId);

        if (cuenta.estado != CuentaEstado.ABIERTA) {
            throw new BusinessException(409, "La cuenta ya está cerrada");
        }

        // Determine next round number
        long rondas = Pedido.count("cuenta.id", cuentaId);
        int numeroRonda = (int) rondas + 1;

        Pedido pedido = new Pedido();
        pedido.cuenta = cuenta;
        pedido.mesero = meseroId != null
                ? (Usuario) Usuario.findById(meseroId).orElse(null) : null;
        pedido.numeroRonda = numeroRonda;
        pedido.persist();

        BigDecimal totalRonda = BigDecimal.ZERO;

        for (CreatePedidoRequest.ItemRequest itemReq : request.items) {
            Producto producto = Producto.findActivoById(itemReq.productoId)
                    .orElseThrow(() -> new BusinessException(404,
                            "Producto no encontrado o inactivo: " + itemReq.productoId));

            ItemPedido item = new ItemPedido();
            item.pedido = pedido;
            item.producto = producto;
            item.cantidad = itemReq.cantidad;
            item.precioUnitario = producto.precio; // snapshot at order time
            item.modificadores = itemReq.modificadores;
            item.estado = ItemPedidoEstado.PENDIENTE;
            item.persist();

            totalRonda = totalRonda.add(producto.precio.multiply(BigDecimal.valueOf(itemReq.cantidad)));

            LOG.infof("ItemPedido added: producto=%s cantidad=%d cuentaId=%s",
                    producto.nombre, itemReq.cantidad, cuentaId);
        }

        // Update cuenta total
        cuenta.total = (cuenta.total != null ? cuenta.total : BigDecimal.ZERO).add(totalRonda);

        // Reload pedido with items
        Pedido saved = (Pedido) Pedido.findById(pedido.id);
        return PedidoDTO.from(saved);
    }

    // -------------------------------------------------------
    // Remove item (pre-send)
    // -------------------------------------------------------

    @Transactional
    public void removeItem(UUID cuentaId, UUID pedidoId, UUID itemId, UUID requesterId) {
        findCuentaOrThrow(cuentaId);

        ItemPedido item = ItemPedido.findByIdOptional(itemId)
                .map(i -> (ItemPedido) i)
                .orElseThrow(() -> new BusinessException(404, "Item no encontrado: " + itemId));

        if (item.estado != ItemPedidoEstado.PENDIENTE) {
            throw new BusinessException(409,
                    "No se puede eliminar un ítem que ya fue enviado a preparación");
        }

        // Release inventory reservation
        inventarioService.liberarReserva(itemId);

        // Update cuenta total
        Cuenta cuenta = findCuentaOrThrow(cuentaId);
        BigDecimal itemTotal = item.precioUnitario.multiply(BigDecimal.valueOf(item.cantidad));
        cuenta.total = cuenta.total.subtract(itemTotal);
        if (cuenta.total.compareTo(BigDecimal.ZERO) < 0) {
            cuenta.total = BigDecimal.ZERO;
        }

        item.delete();
        LOG.infof("ItemPedido removed: itemId=%s cuentaId=%s", itemId, cuentaId);
    }

    // -------------------------------------------------------
    // Entregar item (mesero marca como entregado)
    // -------------------------------------------------------

    @Transactional
    public void entregarItem(UUID cuentaId, UUID itemId, UUID meseroId) {
        findCuentaOrThrow(cuentaId);
        ItemPedido item = ItemPedido.findByIdOptional(itemId)
                .map(i -> (ItemPedido) i)
                .orElseThrow(() -> new BusinessException(404, "Item no encontrado: " + itemId));

        if (item.estado != ItemPedidoEstado.LISTO) {
            throw new BusinessException(409,
                    "Solo se pueden entregar items en estado LISTO. Estado actual: " + item.estado);
        }

        item.estado = ItemPedidoEstado.ENTREGADO;
        item.entregadoEn = java.time.LocalDateTime.now();

        LOG.infof("Item entregado: itemId=%s cuentaId=%s", itemId, cuentaId);
    }

    // -------------------------------------------------------
    // Cobro
    // -------------------------------------------------------

    @Transactional
    public ComprobanteDTO cobrar(UUID cuentaId, CobroRequest request, UUID meseroId) {
        Cuenta cuenta = findCuentaOrThrow(cuentaId);

        if (cuenta.estado != CuentaEstado.ABIERTA) {
            throw new BusinessException(409, "La cuenta ya está cerrada");
        }

        // Validate cash payment
        if (request.metodoPago == CobroRequest.MetodoPago.EFECTIVO) {
            if (request.montoRecibido == null) {
                throw new BusinessException(400, "El monto recibido es requerido para pagos en efectivo");
            }
            if (request.montoRecibido.compareTo(cuenta.total) < 0) {
                throw new BusinessException(400, "El monto recibido es insuficiente");
            }
        }

        // Get all items for comprobante
        List<ItemPedido> allItems = ItemPedido.findByCuentaId(cuentaId);

        // Confirm inventory reservations → SALIDA_VENTA
        for (ItemPedido item : allItems) {
            inventarioService.confirmarReserva(item.id, meseroId);
        }

        // Close cuenta
        cuenta.estado = CuentaEstado.CERRADA;
        cuenta.cerradaEn = LocalDateTime.now();
        cuenta.metodoPago = request.metodoPago.name();

        // Free mesa
        if (cuenta.mesa != null) {
            Mesa mesa = (Mesa) Mesa.findById(cuenta.mesa.id);
            if (mesa != null) {
                mesa.estado = MesaEstado.LIBRE;
                mesa.updatedAt = LocalDateTime.now();
            }
        }

        // Build comprobante
        BigDecimal subtotal = cuenta.total.divide(
                BigDecimal.ONE.add(TAX_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal impuestos = cuenta.total.subtract(subtotal);

        ComprobanteDTO comprobante = new ComprobanteDTO();
        comprobante.numeroCuenta = cuenta.id;
        comprobante.mesa = cuenta.mesa != null ? cuenta.mesa.nombre : null;
        comprobante.items = allItems.stream().map(ItemPedidoDTO::from).collect(Collectors.toList());
        comprobante.subtotal = subtotal;
        comprobante.impuestos = impuestos;
        comprobante.total = cuenta.total;
        comprobante.metodoPago = request.metodoPago.name();
        comprobante.cerradaEn = cuenta.cerradaEn;

        if (request.metodoPago == CobroRequest.MetodoPago.EFECTIVO) {
            comprobante.cambio = request.montoRecibido.subtract(cuenta.total);
        }

        LOG.infof("Cuenta cobrada: cuentaId=%s total=%s metodoPago=%s",
                cuentaId, cuenta.total, request.metodoPago);
        return comprobante;
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private Cuenta findCuentaOrThrow(UUID id) {
        return Cuenta.findByIdOptional(id)
                .map(c -> (Cuenta) c)
                .orElseThrow(() -> new BusinessException(404, "Cuenta no encontrada: " + id));
    }
}
