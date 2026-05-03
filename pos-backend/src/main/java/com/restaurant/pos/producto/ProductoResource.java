package com.restaurant.pos.producto;

import com.restaurant.pos.auth.RequiresRole;
import com.restaurant.pos.common.PaginationParams;
import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @Inject
    ProductoService productoService;

    @GET
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public List<ProductoDTO> findAll(
            @BeanParam PaginationParams pagination,
            @QueryParam("soloActivos") @DefaultValue("true") boolean soloActivos) {
        return productoService.findAll(pagination, soloActivos);
    }

    @GET
    @Path("/{id}")
    @RequiresRole({Rol.MESERO, Rol.ADMIN})
    public ProductoDTO findById(@PathParam("id") UUID id) {
        return productoService.findById(id);
    }

    @POST
    @RequiresRole(Rol.ADMIN)
    public Response create(@Valid CreateProductoRequest request) {
        ProductoDTO dto = productoService.create(request);
        return Response.status(201).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public ProductoDTO update(@PathParam("id") UUID id,
                               UpdateProductoRequest request) {
        return productoService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RequiresRole(Rol.ADMIN)
    public Response deactivate(@PathParam("id") UUID id) {
        productoService.deactivate(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/imagen")
    @RequiresRole(Rol.ADMIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImagen(@PathParam("id") UUID id,
                                  @RestForm("imagen") FileUpload file) {
        try {
            InputStream stream = Files.newInputStream(file.uploadedFile());
            String contentType = file.contentType();
            long fileSize = Files.size(file.uploadedFile());
            String originalFilename = file.fileName();

            ProductoDTO dto = productoService.uploadImagen(
                    id, stream, contentType, fileSize, originalFilename);
            return Response.ok(dto).build();
        } catch (IOException e) {
            return Response.serverError()
                    .entity("{\"message\":\"Error al procesar la imagen\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/imagen")
    @RequiresRole(Rol.ADMIN)
    public ProductoDTO deleteImagen(@PathParam("id") UUID id) {
        return productoService.deleteImagen(id);
    }
}
