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
            @QueryParam("hasta") String hasta) {
        return reporteService.getReporteVentas(parseDate(desde), parseDate(hasta));
    }

    @GET
    @Path("/productos")
    public List<ReporteProductoItemDTO> getProductos(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return reporteService.getReporteProductos(parseDate(desde), parseDate(hasta));
    }

    @GET
    @Path("/inventario")
    public Map<String, Object> getInventario(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return reporteService.getReporteInventario(parseDate(desde), parseDate(hasta));
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
