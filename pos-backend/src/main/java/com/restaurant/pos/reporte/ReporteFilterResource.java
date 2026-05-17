package com.restaurant.pos.reporte;

import com.restaurant.pos.usuario.UsuarioDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/v1/reportes/filtros")
@Produces(MediaType.APPLICATION_JSON)
public class ReporteFilterResource {

    private static final Logger LOG = Logger.getLogger(ReporteFilterResource.class);

    @Inject
    ReporteService reporteService;

    @GET
    @Path("/turnos")
    public List<String> getTurnos() {
        LOG.debug("Obteniendo turnos disponibles");
        return reporteService.obtenerTurnos();
    }

    @GET
    @Path("/meseros")
    public List<UsuarioDTO> getMeseros() {
        LOG.debug("Obteniendo meseros disponibles");
        return reporteService.obtenerMeseros();
    }
}
