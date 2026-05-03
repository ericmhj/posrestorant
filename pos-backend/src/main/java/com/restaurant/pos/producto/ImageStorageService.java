package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class ImageStorageService {

    private static final Logger LOG = Logger.getLogger(ImageStorageService.class);
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @ConfigProperty(name = "pos.uploads.dir", defaultValue = "/workspace/uploads/productos")
    String uploadsDir;

    /**
     * Saves an uploaded image and returns the relative URL path.
     */
    public String save(InputStream imageStream, String originalFilename,
                       String contentType, long fileSize) {
        validateContentType(contentType);
        validateFileSize(fileSize);

        String extension = extractExtension(originalFilename, contentType);
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Path dir = Paths.get(uploadsDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(imageStream, target, StandardCopyOption.REPLACE_EXISTING);
            LOG.infof("Image saved: %s", filename);
            return "/uploads/productos/" + filename;
        } catch (IOException e) {
            LOG.errorf("Failed to save image: %s", e.getMessage());
            throw new BusinessException(500, "Error al guardar la imagen");
        }
    }

    /**
     * Deletes an image file given its URL path.
     */
    public void delete(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) return;
        try {
            // Extract filename from URL: /uploads/productos/filename.ext
            String filename = imagenUrl.substring(imagenUrl.lastIndexOf('/') + 1);
            Path file = Paths.get(uploadsDir, filename);
            Files.deleteIfExists(file);
            LOG.infof("Image deleted: %s", filename);
        } catch (IOException e) {
            LOG.warnf("Could not delete image file: %s — %s", imagenUrl, e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------

    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(400,
                    "Formato de imagen no permitido. Use JPG, PNG o WebP.");
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize > MAX_SIZE_BYTES) {
            throw new BusinessException(400,
                    "La imagen excede el tamaño máximo de 2MB.");
        }
    }

    private String extractExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            if (ALLOWED_EXTENSIONS.contains(ext)) return ext;
        }
        // Fallback from content type
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
