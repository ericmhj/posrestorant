package com.restaurant.pos.inventario;

import com.restaurant.pos.auth.CurrentUser;
import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.common.PaginationParams;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/inventario")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventarioResource {

    @Inject
    InventarioService inventarioService;

    @Inject
    CurrentUser currentUser;

    @GET
    @RequiresRole(Rol.ADMIN)
    public List<ItemInventarioDTO> findAll(@BeanParam PaginationParams pagination) {
        return inventarioService.findAll(pagination);
    }

    @POST
    @RequiresRole(Rol.ADMIN)
    public Response create(@Valid CreateItemInventarioRequest request) {
        ItemInventarioDTO dto = inventarioService.create(request);
        return Response.status(201).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public ItemInventarioDTO update(@PathParam("id") UUID id,
                                    CreateItemInventarioRequest request) {
        return inventarioService.update(id, request);
    }

    @POST
    @Path("/{id}/movimientos")
    @RequiresRole(Rol.ADMIN)
    public Response registrarMovimiento(@PathParam("id") UUID id,
                                         @Valid RegistrarMovimientoRequest request) {
        MovimientoInventarioDTO dto = inventarioService.registrarMovimiento(
                id, request, currentUser.getId());
        return Response.status(201).entity(dto).build();
    }

    @GET
    @Path("/{id}/movimientos")
    @RequiresRole(Rol.ADMIN)
    public List<MovimientoInventarioDTO> getMovimientos(
            @PathParam("id") UUID id,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @BeanParam PaginationParams pagination) {

        LocalDateTime desdeDate = desde != null ? LocalDateTime.parse(desde) : null;
        LocalDateTime hastaDate = hasta != null ? LocalDateTime.parse(hasta) : null;
        return inventarioService.getMovimientos(id, desdeDate, hastaDate, pagination);
    }
}
