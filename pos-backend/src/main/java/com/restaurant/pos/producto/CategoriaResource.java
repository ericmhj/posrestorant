package com.restaurant.pos.producto;

import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    CategoriaService categoriaService;

    @GET
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public List<CategoriaDTO> findAll() {
        return categoriaService.findAll();
    }

    @POST
    @RequiresRole(Rol.ADMIN)
    public Response create(@Valid CreateCategoriaRequest request) {
        CategoriaDTO dto = categoriaService.create(request);
        return Response.status(201).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public CategoriaDTO update(@PathParam("id") UUID id,
                                @Valid CreateCategoriaRequest request) {
        return categoriaService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public Response delete(@PathParam("id") UUID id) {
        categoriaService.delete(id);
        return Response.noContent().build();
    }
}
