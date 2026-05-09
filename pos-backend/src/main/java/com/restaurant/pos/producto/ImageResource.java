package com.restaurant.pos.producto;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/uploads/productos")
public class ImageResource {

    @ConfigProperty(name = "pos.uploads.dir", defaultValue = "/workspace/uploads/productos")
    String uploadsDir;

    @GET
    @Path("/{filename}")
    public Response getImage(@PathParam("filename") String filename) {
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/")) {
            return Response.status(400).build();
        }

        java.nio.file.Path file = Paths.get(uploadsDir, filename);
        if (!Files.exists(file)) {
            return Response.status(404).build();
        }

        String contentType = detectContentType(filename);
        StreamingOutput stream = output -> {
            try (var in = Files.newInputStream(file)) {
                in.transferTo(output);
            } catch (IOException e) {
                throw new IOException("Error reading image", e);
            }
        };

        return Response.ok(stream, contentType)
                .header("Cache-Control", "public, max-age=86400")
                .build();
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
