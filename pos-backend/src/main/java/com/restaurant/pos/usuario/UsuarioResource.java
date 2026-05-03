package com.restaurant.pos.usuario;

import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.common.PaginationParams;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    @RequiresRole(Rol.ADMIN)
    public List<UsuarioDTO> findAll(@BeanParam PaginationParams pagination) {
        return usuarioService.findAll(pagination);
    }

    @POST
    @RequiresRole(Rol.ADMIN)
    public Response create(@Valid CreateUsuarioRequest request) {
        UsuarioDTO dto = usuarioService.create(request);
        return Response.status(201).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public UsuarioDTO update(@PathParam("id") UUID id, UpdateUsuarioRequest request) {
        return usuarioService.update(id, request);
    }

    @PUT
    @Path("/{id}/desactivar")
    @RequiresRole(Rol.ADMIN)
    public Response deactivate(@PathParam("id") UUID id) {
        usuarioService.deactivate(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/reset-password")
    @RequiresRole(Rol.ADMIN)
    public Response resetPassword(@PathParam("id") UUID id, ResetPasswordRequest request) {
        usuarioService.resetPassword(id, request.newPassword);
        return Response.noContent().build();
    }

    public static class ResetPasswordRequest {
        public String newPassword;
    }
}
