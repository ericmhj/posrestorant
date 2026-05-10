package com.restaurant.pos.cuenta;

import com.restaurant.pos.auth.CurrentUser;
import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/cuentas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuentaResource {

    @Inject
    CuentaService cuentaService;

    @Inject
    CurrentUser currentUser;

    @GET
    @Path("/{id}")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public CuentaDetalleDTO findById(@PathParam("id") UUID id) {
        return cuentaService.findById(id);
    }

    @POST
    @Path("/{id}/pedidos")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public Response addPedido(@PathParam("id") UUID id,
                               @Valid CreatePedidoRequest request) {
        PedidoDTO dto = cuentaService.addPedido(id, request, currentUser.getId());
        return Response.status(201).entity(dto).build();
    }

    @DELETE
    @Path("/{cuentaId}/pedidos/{pedidoId}/items/{itemId}")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public Response removeItem(@PathParam("cuentaId") UUID cuentaId,
                                @PathParam("pedidoId") UUID pedidoId,
                                @PathParam("itemId") UUID itemId) {
        cuentaService.removeItem(cuentaId, pedidoId, itemId, currentUser.getId());
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/cobro")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public Response cobrar(@PathParam("id") UUID id,
                            @Valid CobroRequest request) {
        ComprobanteDTO comprobante = cuentaService.cobrar(id, request, currentUser.getId());
        return Response.ok(comprobante).build();
    }

    @PUT
    @Path("/{cuentaId}/items/{itemId}/entregar")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public Response entregarItem(@PathParam("cuentaId") UUID cuentaId,
                                  @PathParam("itemId") UUID itemId) {
        cuentaService.entregarItem(cuentaId, itemId, currentUser.getId());
        return Response.noContent().build();
    }
}
