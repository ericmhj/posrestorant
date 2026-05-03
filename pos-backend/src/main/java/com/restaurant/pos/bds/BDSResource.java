package com.restaurant.pos.bds;

import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.pedido.ItemPedidoDTO;
import com.restaurant.pos.pedido.ItemPedidoEstado;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/bds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BDSResource {

    @Inject
    BDSService bdsService;

    @GET
    @Path("/items")
    @RequiresRole({Rol.BARRA, Rol.ADMIN})
    public List<ItemPedidoDTO> findItems() {
        return bdsService.findItems();
    }

    @PUT
    @Path("/items/{id}/estado")
    @RequiresRole({Rol.BARRA, Rol.ADMIN})
    public ItemPedidoDTO updateEstado(@PathParam("id") UUID id,
                                       UpdateEstadoRequest request) {
        return bdsService.updateEstado(id, request.estado, null);
    }

    public static class UpdateEstadoRequest {
        public ItemPedidoEstado estado;
    }
}
