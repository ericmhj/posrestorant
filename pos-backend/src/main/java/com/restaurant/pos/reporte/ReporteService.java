package com.restaurant.pos.reporte;

import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
import com.restaurant.pos.inventario.ItemInventario;
import com.restaurant.pos.inventario.ItemInventarioDTO;
import com.restaurant.pos.inventario.MovimientoInventario;
import com.restaurant.pos.inventario.MovimientoInventarioDTO;
import com.restaurant.pos.inventario.ReservaInventario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReporteService {

    private static final Logger LOG = Logger.getLogger(ReporteService.class);

    @Inject
    EntityManager em;

    // -------------------------------------------------------
    // Reporte de Ventas
    // -------------------------------------------------------

    public ReporteVentasDTO getReporteVentas(LocalDateTime desde, LocalDateTime hasta) {
        ReporteVentasDTO reporte = new ReporteVentasDTO();
        reporte.desde = desde;
        reporte.hasta = hasta;

        List<Cuenta> cuentas = em.createQuery(
                "SELECT c FROM Cuenta c WHERE c.estado = :estado " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta",
                Cuenta.class)
                .setParameter("estado", CuentaEstado.CERRADA)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        if (cuentas.isEmpty()) {
            return reporte; // zeros
        }

        reporte.numeroCuentas = (long) cuentas.size();
        reporte.totalVentas = cuentas.stream()
                .map(c -> c.total != null ? c.total : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        reporte.ticketPromedio = reporte.totalVentas.divide(
                BigDecimal.valueOf(reporte.numeroCuentas), 2, RoundingMode.HALF_UP);

        // Desglose por método de pago
        Map<String, BigDecimal> desglose = new HashMap<>();
        for (Cuenta c : cuentas) {
            String metodo = c.metodoPago != null ? c.metodoPago : "DESCONOCIDO";
            desglose.merge(metodo, c.total != null ? c.total : BigDecimal.ZERO, BigDecimal::add);
        }
        reporte.desglosePago = desglose;

        return reporte;
    }

    // -------------------------------------------------------
    // Reporte de Productos más vendidos
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteProductoItemDTO> getReporteProductos(LocalDateTime desde, LocalDateTime hasta) {
        List<Object[]> results = em.createQuery(
                "SELECT p.nombre, SUM(ip.cantidad), SUM(ip.precioUnitario * ip.cantidad), cat.nombre " +
                "FROM ItemPedido ip " +
                "JOIN ip.producto p " +
                "JOIN p.categoria cat " +
                "JOIN ip.pedido ped " +
                "JOIN ped.cuenta c " +
                "WHERE c.estado = 'CERRADA' " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta " +
                "GROUP BY p.nombre, cat.nombre " +
                "ORDER BY SUM(ip.cantidad) DESC")
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        return results.stream()
                .map(r -> new ReporteProductoItemDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2],
                        (String) r[3]))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Reporte de Inventario
    // -------------------------------------------------------

    public Map<String, Object> getReporteInventario(LocalDateTime desde, LocalDateTime hasta) {
        List<ItemInventario> items = ItemInventario.listAll();
        List<ItemInventarioDTO> itemDTOs = items.stream()
                .map(item -> {
                    BigDecimal reservado = ReservaInventario.sumReservasByItemInventarioId(item.id);
                    return ItemInventarioDTO.from(item, reservado);
                })
                .collect(Collectors.toList());

        List<MovimientoInventarioDTO> movimientos = MovimientoInventario
                .findByItemAndPeriod(null, desde, hasta, 0, 1000)
                .stream()
                .map(MovimientoInventarioDTO::from)
                .collect(Collectors.toList());

        List<ItemInventarioDTO> alertas = itemDTOs.stream()
                .filter(i -> Boolean.TRUE.equals(i.alertaMinimo))
                .collect(Collectors.toList());

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("items", itemDTOs);
        reporte.put("movimientos", movimientos);
        reporte.put("alertasActivas", alertas);
        return reporte;
    }

    // -------------------------------------------------------
    // Reporte de Operación
    // -------------------------------------------------------

    public ReporteOperacionDTO getReporteOperacion(LocalDateTime desde, LocalDateTime hasta) {
        ReporteOperacionDTO reporte = new ReporteOperacionDTO();

        // Total pedidos in period
        reporte.totalPedidos = em.createQuery(
                "SELECT COUNT(p) FROM Pedido p " +
                "JOIN p.cuenta c WHERE c.cerradaEn >= :desde AND c.cerradaEn <= :hasta",
                Long.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();

        // Avg preparation time for COCINA (preparandoEn - createdAt in seconds)
        Double avgCocina = em.createQuery(
                "SELECT AVG(FUNCTION('EXTRACT', 'EPOCH' FROM ip.preparandoEn) - " +
                "FUNCTION('EXTRACT', 'EPOCH' FROM ip.createdAt)) " +
                "FROM ItemPedido ip " +
                "JOIN ip.producto p " +
                "WHERE p.estacion = 'COCINA' AND ip.preparandoEn IS NOT NULL " +
                "AND ip.createdAt >= :desde AND ip.createdAt <= :hasta",
                Double.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        reporte.tiempoPromedioCocinaSegundos = avgCocina != null ? avgCocina : 0.0;

        // Avg preparation time for BARRA
        Double avgBarra = em.createQuery(
                "SELECT AVG(FUNCTION('EXTRACT', 'EPOCH' FROM ip.preparandoEn) - " +
                "FUNCTION('EXTRACT', 'EPOCH' FROM ip.createdAt)) " +
                "FROM ItemPedido ip " +
                "JOIN ip.producto p " +
                "WHERE p.estacion = 'BARRA' AND ip.preparandoEn IS NOT NULL " +
                "AND ip.createdAt >= :desde AND ip.createdAt <= :hasta",
                Double.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        reporte.tiempoPromedioBarraSegundos = avgBarra != null ? avgBarra : 0.0;

        return reporte;
    }

    // -------------------------------------------------------
    // CSV Export
    // -------------------------------------------------------

    public byte[] exportVentasCSV(LocalDateTime desde, LocalDateTime hasta) {
        ReporteVentasDTO reporte = getReporteVentas(desde, hasta);
        StringBuilder sb = new StringBuilder();
        sb.append("Periodo,Total Ventas,Numero Cuentas,Ticket Promedio\n");
        sb.append(String.format("%s a %s,%s,%d,%s\n",
                desde, hasta, reporte.totalVentas,
                reporte.numeroCuentas, reporte.ticketPromedio));
        sb.append("\nMetodo Pago,Total\n");
        reporte.desglosePago.forEach((k, v) ->
                sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportProductosCSV(LocalDateTime desde, LocalDateTime hasta) {
        List<ReporteProductoItemDTO> items = getReporteProductos(desde, hasta);
        StringBuilder sb = new StringBuilder();
        sb.append("Producto,Cantidad Vendida,Ingresos,Categoria\n");
        items.forEach(i -> sb.append(String.format("%s,%d,%s,%s\n",
                i.nombre, i.cantidadVendida, i.ingresos, i.categoria)));
        return sb.toString().getBytes();
    }
}
