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

import com.restaurant.pos.usuario.Usuario;
import com.restaurant.pos.usuario.UsuarioDTO;

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

    public ReporteVentasDTO getReporteVentas(ReporteFiltro filtro) {
        ReporteVentasDTO reporte = new ReporteVentasDTO();
        reporte.desde = filtro.desde;
        reporte.hasta = filtro.hasta;

        StringBuilder jpql = new StringBuilder(
                "SELECT c FROM Cuenta c WHERE c.estado = :estado " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta");

        if (filtro.meseroId != null) {
            jpql.append(" AND c.mesero.id = :meseroId");
        }
        if (filtro.turno != null) {
            int[] horas = filtro.getHorasRango();
            if (horas != null && horas[0] < horas[1]) {
                jpql.append(" AND FUNCTION('EXTRACT', HOUR FROM c.cerradaEn) >= ").append(horas[0]);
                jpql.append(" AND FUNCTION('EXTRACT', HOUR FROM c.cerradaEn) < ").append(horas[1]);
            }
        }

        var query = em.createQuery(jpql.toString(), Cuenta.class)
                .setParameter("estado", CuentaEstado.CERRADA)
                .setParameter("desde", filtro.desde)
                .setParameter("hasta", filtro.hasta);

        if (filtro.meseroId != null) {
            query.setParameter("meseroId", filtro.meseroId);
        }

        List<Cuenta> cuentas = query.getResultList();

        if (cuentas.isEmpty()) {
            return reporte;
        }

        reporte.numeroCuentas = (long) cuentas.size();
        reporte.totalVentas = cuentas.stream()
                .map(c -> c.total != null ? c.total : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        reporte.ticketPromedio = reporte.totalVentas.divide(
                BigDecimal.valueOf(reporte.numeroCuentas), 2, RoundingMode.HALF_UP);

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
    public List<ReporteProductoItemDTO> getReporteProductos(ReporteFiltro filtro) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p.nombre, SUM(ip.cantidad), SUM(ip.precioUnitario * ip.cantidad), cat.nombre " +
                "FROM ItemPedido ip " +
                "JOIN ip.producto p " +
                "JOIN p.categoria cat " +
                "JOIN ip.pedido ped " +
                "JOIN ped.cuenta c " +
                "WHERE c.estado = 'CERRADA' " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta");

        if (filtro.estacion != null) {
            jpql.append(" AND p.estacion = '").append(filtro.estacion).append("'");
        }
        if (filtro.categoriaId != null) {
            jpql.append(" AND cat.id = :categoriaId");
        }
        if (filtro.meseroId != null) {
            jpql.append(" AND c.mesero.id = :meseroId");
        }

        jpql.append(" GROUP BY p.nombre, cat.nombre ORDER BY SUM(ip.cantidad) DESC");

        var query = em.createQuery(jpql.toString())
                .setParameter("desde", filtro.desde)
                .setParameter("hasta", filtro.hasta);

        if (filtro.categoriaId != null) {
            query.setParameter("categoriaId", filtro.categoriaId);
        }
        if (filtro.meseroId != null) {
            query.setParameter("meseroId", filtro.meseroId);
        }

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(r -> new ReporteProductoItemDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2],
                        (String) r[3]))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Reporte de Inventario Detallado
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteInventarioItemDTO> getReporteInventarioDetallado(ReporteFiltro filtro) {
        // Obtener todos los items de inventario
        StringBuilder jpql = new StringBuilder("SELECT i FROM ItemInventario i");
        if (filtro.categoriaId != null) {
            jpql.append(" WHERE i.categoria.id = :categoriaId");
        }
        jpql.append(" ORDER BY i.nombre");

        var query = em.createQuery(jpql.toString(), com.restaurant.pos.inventario.ItemInventario.class);
        if (filtro.categoriaId != null) {
            query.setParameter("categoriaId", filtro.categoriaId);
        }

        List<com.restaurant.pos.inventario.ItemInventario> items = query.getResultList();

        // Calcular días entre desde y hasta para consumo promedio
        long diasPeriodo = java.time.Duration.between(filtro.desde, filtro.hasta).toDays();
        if (diasPeriodo <= 0) diasPeriodo = 30;

        List<ReporteInventarioItemDTO> resultado = new ArrayList<>();

        for (com.restaurant.pos.inventario.ItemInventario item : items) {
            ReporteInventarioItemDTO dto = new ReporteInventarioItemDTO();
            dto.id = item.id;
            dto.nombre = item.nombre;
            dto.unidadMedida = item.unidadMedida;
            dto.categoria = item.categoria != null ? item.categoria.nombre : null;
            dto.stockActual = item.stockActual;
            dto.stockMinimo = item.stockMinimo;
            dto.costoUnitario = item.costoUnitario != null ? item.costoUnitario : BigDecimal.ZERO;
            dto.costoInventario = dto.stockActual.multiply(dto.costoUnitario);

            // Reservas activas
            BigDecimal reservado = com.restaurant.pos.inventario.ReservaInventario
                    .sumReservasByItemInventarioId(item.id);
            dto.stockDisponible = dto.stockActual.subtract(reservado);

            // Consumo total en el período (movimientos SALIDA_VENTA)
            BigDecimal consumoTotal = getConsumoEnPeriodo(item.id, filtro.desde, filtro.hasta);
            dto.consumoPromedioDiario = consumoTotal.divide(
                    BigDecimal.valueOf(diasPeriodo), 3, RoundingMode.HALF_UP);

            // Proyección a 7 días
            BigDecimal consumoSemana = dto.consumoPromedioDiario.multiply(BigDecimal.valueOf(7));
            dto.stockProyectadoSemana = dto.stockActual.subtract(consumoSemana);

            // Rotación: días desde última salida
            dto.rotacionDias = getDiasDesdeUltimaSalida(item.id);

            // Alertas
            dto.alertaMinimo = dto.stockActual.compareTo(dto.stockMinimo) < 0;
            dto.alertaProyeccion = dto.stockProyectadoSemana.compareTo(dto.stockMinimo) < 0;

            resultado.add(dto);
        }

        return resultado;
    }

    private BigDecimal getConsumoEnPeriodo(java.util.UUID itemId, LocalDateTime desde, LocalDateTime hasta) {
        Object result = em.createQuery(
                "SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
                "WHERE m.itemInventario.id = :itemId AND m.tipo = 'SALIDA_VENTA' " +
                "AND m.fechaHora >= :desde AND m.fechaHora <= :hasta")
                .setParameter("itemId", itemId)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        return result != null ? (BigDecimal) result : BigDecimal.ZERO;
    }

    private Double getDiasDesdeUltimaSalida(java.util.UUID itemId) {
        List<LocalDateTime> results = em.createQuery(
                "SELECT MAX(m.fechaHora) FROM MovimientoInventario m " +
                "WHERE m.itemInventario.id = :itemId AND m.tipo = 'SALIDA_VENTA'",
                LocalDateTime.class)
                .setParameter("itemId", itemId)
                .getResultList();

        if (results.isEmpty() || results.get(0) == null) return null;
        return (double) java.time.Duration.between(results.get(0), LocalDateTime.now()).toDays();
    }

    // -------------------------------------------------------
    // Reporte de Desempeño de Estaciones
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteEstacionItemDTO> getReporteEstaciones(ReporteFiltro filtro) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.nombre, p.estacion, COUNT(ip.id), " +
                "AVG(EXTRACT(EPOCH FROM (ip.listo_en - ip.created_at))), " +
                "MIN(EXTRACT(EPOCH FROM (ip.listo_en - ip.created_at))), " +
                "MAX(EXTRACT(EPOCH FROM (ip.listo_en - ip.created_at))) " +
                "FROM item_pedido ip " +
                "JOIN producto p ON ip.producto_id = p.id " +
                "WHERE ip.listo_en IS NOT NULL " +
                "AND ip.created_at >= ?1 AND ip.created_at <= ?2");

        if (filtro.estacion != null) {
            sql.append(" AND p.estacion = '").append(filtro.estacion).append("'");
        }

        sql.append(" GROUP BY p.nombre, p.estacion ORDER BY AVG(EXTRACT(EPOCH FROM (ip.listo_en - ip.created_at))) DESC");

        List<Object[]> results = em.createNativeQuery(sql.toString())
                .setParameter(1, filtro.desde)
                .setParameter(2, filtro.hasta)
                .getResultList();

        Double maxTiempo = results.stream()
                .mapToDouble(r -> r[3] != null ? ((Number) r[3]).doubleValue() : 0.0)
                .max().orElse(0.0);

        return results.stream().map(r -> {
            ReporteEstacionItemDTO dto = new ReporteEstacionItemDTO();
            dto.producto = (String) r[0];
            dto.estacion = (String) r[1];
            dto.cantidadPreparada = ((Number) r[2]).longValue();
            dto.tiempoPromedioSegundos = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;
            dto.tiempoPromedioMinutos = dto.tiempoPromedioSegundos / 60.0;
            dto.tiempoMinSegundos = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            dto.tiempoMaxSegundos = r[5] != null ? ((Number) r[5]).doubleValue() : 0.0;
            dto.varianzaSegundos = dto.tiempoMaxSegundos - dto.tiempoMinSegundos;
            dto.esCuelloBotella = dto.tiempoPromedioSegundos >= maxTiempo * 0.9 && maxTiempo > 0;
            return dto;
        }).collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Reporte de Rentabilidad por Producto
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteRentabilidadItemDTO> getReporteRentabilidad(ReporteFiltro filtro) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p.nombre, cat.nombre, SUM(ip.cantidad), SUM(ip.precioUnitario * ip.cantidad), p.id " +
                "FROM ItemPedido ip " +
                "JOIN ip.producto p " +
                "JOIN p.categoria cat " +
                "JOIN ip.pedido ped " +
                "JOIN ped.cuenta c " +
                "WHERE c.estado = 'CERRADA' " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta");

        if (filtro.estacion != null) {
            jpql.append(" AND p.estacion = '").append(filtro.estacion).append("'");
        }
        if (filtro.categoriaId != null) {
            jpql.append(" AND cat.id = :categoriaId");
        }

        jpql.append(" GROUP BY p.nombre, cat.nombre, p.id ORDER BY SUM(ip.precioUnitario * ip.cantidad) DESC");

        var query = em.createQuery(jpql.toString())
                .setParameter("desde", filtro.desde)
                .setParameter("hasta", filtro.hasta);

        if (filtro.categoriaId != null) {
            query.setParameter("categoriaId", filtro.categoriaId);
        }

        List<Object[]> results = query.getResultList();

        List<ReporteRentabilidadItemDTO> items = new ArrayList<>();
        int rank = 1;

        for (Object[] r : results) {
            ReporteRentabilidadItemDTO dto = new ReporteRentabilidadItemDTO();
            dto.nombre = (String) r[0];
            dto.categoria = (String) r[1];
            dto.cantidadVendida = ((Number) r[2]).longValue();
            dto.ingresosBrutos = (BigDecimal) r[3];
            java.util.UUID productoId = (java.util.UUID) r[4];

            // Calcular costo basado en ingredientes vinculados
            BigDecimal costoUnitario = calcularCostoProducto(productoId);
            dto.costoUnitario = costoUnitario;
            dto.costoTotal = costoUnitario.multiply(BigDecimal.valueOf(dto.cantidadVendida));
            dto.margenBruto = dto.ingresosBrutos.subtract(dto.costoTotal);
            dto.margenPorcentaje = dto.ingresosBrutos.compareTo(BigDecimal.ZERO) > 0
                    ? dto.margenBruto.doubleValue() / dto.ingresosBrutos.doubleValue() * 100
                    : 0.0;
            dto.ranking = rank++;
            items.add(dto);
        }

        return items;
    }

    private BigDecimal calcularCostoProducto(java.util.UUID productoId) {
        List<Object[]> ingredientes = em.createQuery(
                "SELECT pi.cantidad, pi.itemInventario.costoUnitario " +
                "FROM ProductoIngrediente pi WHERE pi.producto.id = :productoId")
                .setParameter("productoId", productoId)
                .getResultList();

        BigDecimal costo = BigDecimal.ZERO;
        for (Object[] ing : ingredientes) {
            BigDecimal cantidad = (BigDecimal) ing[0];
            BigDecimal costoUnit = ing[1] != null ? (BigDecimal) ing[1] : BigDecimal.ZERO;
            costo = costo.add(cantidad.multiply(costoUnit));
        }
        return costo.setScale(2, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------
    // Reporte de Horas Pico
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteHoraPicoItemDTO> getReporteHorasPico(ReporteFiltro filtro) {
        List<Object[]> results = em.createNativeQuery(
                "SELECT CAST(EXTRACT(HOUR FROM c.cerrada_en) AS INTEGER), COUNT(c.id), SUM(c.total), " +
                "AVG(EXTRACT(EPOCH FROM (c.cerrada_en - c.abierta_en)) / 60.0) " +
                "FROM cuenta c " +
                "WHERE c.estado = 'CERRADA' " +
                "AND c.cerrada_en >= ?1 AND c.cerrada_en <= ?2 " +
                "GROUP BY CAST(EXTRACT(HOUR FROM c.cerrada_en) AS INTEGER) " +
                "ORDER BY CAST(EXTRACT(HOUR FROM c.cerrada_en) AS INTEGER)")
                .setParameter(1, filtro.desde)
                .setParameter(2, filtro.hasta)
                .getResultList();

        long maxCuentas = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .max().orElse(0);

        return results.stream().map(r -> {
            Integer hora = ((Number) r[0]).intValue();
            Long numCuentas = ((Number) r[1]).longValue();
            BigDecimal ingresos = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            Double tiempoMin = r[3] != null ? ((Number) r[3]).doubleValue() : 0.0;

            ReporteHoraPicoItemDTO dto = new ReporteHoraPicoItemDTO(hora, numCuentas, ingresos, tiempoMin, null);
            dto.esPico = numCuentas == maxCuentas && maxCuentas > 0;
            return dto;
        }).collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Reporte de Desempeño por Mesero
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ReporteMeseroItemDTO> getReporteMeseros(ReporteFiltro filtro) {
        StringBuilder jpql = new StringBuilder(
                "SELECT c.mesero.id, c.mesero.nombre, c.mesero.apellido, " +
                "COUNT(c), SUM(c.total), AVG(c.total) " +
                "FROM Cuenta c " +
                "WHERE c.estado = 'CERRADA' AND c.mesero IS NOT NULL " +
                "AND c.cerradaEn >= :desde AND c.cerradaEn <= :hasta");

        if (filtro.meseroId != null) {
            jpql.append(" AND c.mesero.id = :meseroId");
        }

        jpql.append(" GROUP BY c.mesero.id, c.mesero.nombre, c.mesero.apellido ORDER BY SUM(c.total) DESC");

        var query = em.createQuery(jpql.toString())
                .setParameter("desde", filtro.desde)
                .setParameter("hasta", filtro.hasta);

        if (filtro.meseroId != null) {
            query.setParameter("meseroId", filtro.meseroId);
        }

        List<Object[]> results = query.getResultList();

        return results.stream().map(r -> {
            java.util.UUID meseroId = (java.util.UUID) r[0];
            String nombre = (String) r[1];
            String apellido = (String) r[2];
            Long numCuentas = ((Number) r[3]).longValue();
            BigDecimal ingresos = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;
            BigDecimal ticket = r[5] != null ? new BigDecimal(((Number) r[5]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // Tiempo promedio se calcula con query nativa
            Double tiempoMin = calcularTiempoPromedioMesero(meseroId, filtro.desde, filtro.hasta);

            Long cancelados = 0L;
            Double tasa = 0.0;

            return new ReporteMeseroItemDTO(meseroId, nombre, apellido, numCuentas, ingresos, ticket, tiempoMin, cancelados, tasa);
        }).collect(Collectors.toList());
    }

    private Double calcularTiempoPromedioMesero(java.util.UUID meseroId, LocalDateTime desde, LocalDateTime hasta) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT AVG(EXTRACT(EPOCH FROM (cerrada_en - abierta_en)) / 60.0) " +
                    "FROM cuenta WHERE mesero_id = ?1 AND estado = 'CERRADA' " +
                    "AND cerrada_en >= ?2 AND cerrada_en <= ?3")
                    .setParameter(1, meseroId)
                    .setParameter(2, desde)
                    .setParameter(3, hasta)
                    .getSingleResult();
            return result != null ? ((Number) result).doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
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
        ReporteFiltro filtro = new ReporteFiltro(desde, hasta, null, null, null, null);
        ReporteVentasDTO reporte = getReporteVentas(filtro);
        StringBuilder sb = new StringBuilder();
        sb.append("Periodo,Total Ventas,Numero Cuentas,Ticket Promedio\n");
        sb.append(String.format("%s a %s,%s,%d,%s\n",
                desde, hasta, reporte.totalVentas,
                reporte.numeroCuentas, reporte.ticketPromedio));
        sb.append("\nMetodo Pago,Total\n");
        if (reporte.desglosePago != null) {
            reporte.desglosePago.forEach((k, v) ->
                    sb.append(k).append(",").append(v).append("\n"));
        }
        return sb.toString().getBytes();
    }

    public byte[] exportProductosCSV(LocalDateTime desde, LocalDateTime hasta) {
        ReporteFiltro filtro = new ReporteFiltro(desde, hasta, null, null, null, null);
        List<ReporteProductoItemDTO> items = getReporteProductos(filtro);
        StringBuilder sb = new StringBuilder();
        sb.append("Producto,Cantidad Vendida,Ingresos,Categoria\n");
        items.forEach(i -> sb.append(String.format("%s,%d,%s,%s\n",
                i.nombre, i.cantidadVendida, i.ingresos, i.categoria)));
        return sb.toString().getBytes();
    }

    // -------------------------------------------------------
    // Helpers - Filtros y Validación
    // -------------------------------------------------------

    public boolean validarFiltro(PeriodoFiltroDTO filtro) {
        if (filtro == null || !filtro.esValido()) {
            LOG.warn("Filtro inválido: " + filtro);
            return false;
        }
        if (filtro.turno != null && TurnoEnum.fromString(filtro.turno) == null) {
            LOG.warn("Turno inválido: " + filtro.turno);
            return false;
        }
        return true;
    }

    public List<String> obtenerTurnos() {
        List<String> turnos = new ArrayList<>();
        for (TurnoEnum t : TurnoEnum.values()) {
            turnos.add(t.name());
        }
        return turnos;
    }

    public List<UsuarioDTO> obtenerMeseros() {
        List<Usuario> usuarios = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.rol = 'MESERO' ORDER BY u.nombre",
                Usuario.class)
                .getResultList();

        return usuarios.stream()
                .map(UsuarioDTO::from)
                .collect(Collectors.toList());
    }
}
