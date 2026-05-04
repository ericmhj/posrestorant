package com.restaurant.pos.auth;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    CurrentUser currentUser;

    @POST
    @Path("/login")
    public Response login(@Valid AuthLoginRequest request) {
        AuthLoginResponse response = authService.login(request, "unknown");
        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context ContainerRequestContext requestContext) {
        String token = (String) requestContext.getProperty("authToken");
        if (token != null) {
            authService.logout(token, currentUser.getId());
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/password")
    public Response changePassword(@Valid ChangePasswordRequest request) {
        authService.changePassword(currentUser.getId(), request);
        return Response.noContent().build();
    }
}
