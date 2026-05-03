package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ImageStorageServiceTest {

    @Inject
    ImageStorageService imageStorageService;

    private InputStream fakeStream(int sizeBytes) {
        return new ByteArrayInputStream(new byte[sizeBytes]);
    }

    @Test
    void validJpg_isAccepted() {
        // 1KB fake image
        String url = imageStorageService.save(
                fakeStream(1024), "test.jpg", "image/jpeg", 1024);
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/productos/"));
        assertTrue(url.endsWith(".jpg"));
        // cleanup
        imageStorageService.delete(url);
    }

    @Test
    void validPng_isAccepted() {
        String url = imageStorageService.save(
                fakeStream(512), "test.png", "image/png", 512);
        assertNotNull(url);
        assertTrue(url.endsWith(".png"));
        imageStorageService.delete(url);
    }

    @Test
    void validWebp_isAccepted() {
        String url = imageStorageService.save(
                fakeStream(512), "test.webp", "image/webp", 512);
        assertNotNull(url);
        assertTrue(url.endsWith(".webp"));
        imageStorageService.delete(url);
    }

    @Test
    void invalidFormat_isRejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> imageStorageService.save(
                        fakeStream(512), "test.gif", "image/gif", 512));
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("JPG") || ex.getMessage().contains("formato"));
    }

    @Test
    void fileLargerThan2MB_isRejected() {
        long oversized = 2 * 1024 * 1024 + 1; // 2MB + 1 byte
        BusinessException ex = assertThrows(BusinessException.class,
                () -> imageStorageService.save(
                        fakeStream(100), "big.jpg", "image/jpeg", oversized));
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("2MB") || ex.getMessage().contains("tamaño"));
    }

    @Test
    void exactly2MB_isAccepted() {
        long exactly2MB = 2 * 1024 * 1024;
        String url = imageStorageService.save(
                fakeStream(100), "exact.jpg", "image/jpeg", exactly2MB);
        assertNotNull(url);
        imageStorageService.delete(url);
    }

    @Test
    void uniqueFilenameGeneratedPerUpload() {
        String url1 = imageStorageService.save(
                fakeStream(512), "same.jpg", "image/jpeg", 512);
        String url2 = imageStorageService.save(
                fakeStream(512), "same.jpg", "image/jpeg", 512);
        assertNotEquals(url1, url2, "Each upload must generate a unique filename");
        imageStorageService.delete(url1);
        imageStorageService.delete(url2);
    }
}
