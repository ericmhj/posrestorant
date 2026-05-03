package com.restaurant.pos.mesa;

import com.restaurant.pos.auth.CurrentUser;
import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaDTO;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/mesas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MesaResource {

    @Inject
    MesaService mesaService;

    @Inject
    CurrentUser currentUser;

    @GET
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public List<MesaDTO> findAll() {
        return mesaService.findAll();
    }

    @POST
    @RequiresRole(Rol.ADMIN)
    public Response create(@Valid CreateMesaRequest request) {
        MesaDTO dto = mesaService.create(request);
        return Response.status(201).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public MesaDTO update(@PathParam("id") UUID id, @Valid CreateMesaRequest request) {
        return mesaService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public Response delete(@PathParam("id") UUID id) {
        mesaService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/abrir")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public Response abrir(@PathParam("id") UUID id) {
        Cuenta cuenta = mesaService.abrir(id, currentUser.getId());
        return Response.status(201).entity(CuentaDTO.from(cuenta)).build();
    }
}
