package com.restaurant.pos.reporte;

import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/api/v1/reportes")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole(Rol.ADMIN)
public class ReporteResource {

    @Inject
    ReporteService reporteService;

    @GET
    @Path("/ventas")
    public ReporteVentasDTO getVentas(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("turno") String turno,
            @QueryParam("meseroId") String meseroId,
            @QueryParam("estacion") String estacion,
            @QueryParam("categoriaId") String categoriaId) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), turno, meseroId, estacion, categoriaId);
        return reporteService.getReporteVentas(filtro);
    }

    @GET
    @Path("/productos")
    public List<ReporteProductoItemDTO> getProductos(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("turno") String turno,
            @QueryParam("meseroId") String meseroId,
            @QueryParam("estacion") String estacion,
            @QueryParam("categoriaId") String categoriaId) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), turno, meseroId, estacion, categoriaId);
        return reporteService.getReporteProductos(filtro);
    }

    @GET
    @Path("/inventario")
    public Map<String, Object> getInventario(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return reporteService.getReporteInventario(parseDate(desde), parseDate(hasta));
    }

    @GET
    @Path("/meseros")
    public List<ReporteMeseroItemDTO> getMeseros(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("turno") String turno,
            @QueryParam("meseroId") String meseroId) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), turno, meseroId, null, null);
        return reporteService.getReporteMeseros(filtro);
    }

    @GET
    @Path("/horas-pico")
    public List<ReporteHoraPicoItemDTO> getHorasPico(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("turno") String turno,
            @QueryParam("estacion") String estacion) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), turno, null, estacion, null);
        return reporteService.getReporteHorasPico(filtro);
    }

    @GET
    @Path("/rentabilidad")
    public List<ReporteRentabilidadItemDTO> getRentabilidad(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("estacion") String estacion,
            @QueryParam("categoriaId") String categoriaId) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), null, null, estacion, categoriaId);
        return reporteService.getReporteRentabilidad(filtro);
    }

    @GET
    @Path("/estaciones")
    public List<ReporteEstacionItemDTO> getEstaciones(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("estacion") String estacion) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), null, null, estacion, null);
        return reporteService.getReporteEstaciones(filtro);
    }

    @GET
    @Path("/inventario-detallado")
    public List<ReporteInventarioItemDTO> getInventarioDetallado(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("categoriaId") String categoriaId) {
        ReporteFiltro filtro = new ReporteFiltro(parseDate(desde), parseDate(hasta), null, null, null, categoriaId);
        return reporteService.getReporteInventarioDetallado(filtro);
    }

    @GET
    @Path("/operacion")
    public ReporteOperacionDTO getOperacion(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return reporteService.getReporteOperacion(parseDate(desde), parseDate(hasta));
    }

    @GET
    @Path("/ventas/export")
    @Produces("text/csv")
    public Response exportVentas(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        byte[] csv = reporteService.exportVentasCSV(parseDate(desde), parseDate(hasta));
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"ventas.csv\"")
                .build();
    }

    @GET
    @Path("/productos/export")
    @Produces("text/csv")
    public Response exportProductos(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        byte[] csv = reporteService.exportProductosCSV(parseDate(desde), parseDate(hasta));
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"productos.csv\"")
                .build();
    }

    private LocalDateTime parseDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDateTime.now().minusMonths(1);
        }
        return LocalDateTime.parse(date);
    }
}
