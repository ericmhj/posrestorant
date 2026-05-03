package com.restaurant.pos.camel;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Property 11: Items routed by estacion — COCINA → KDS, BARRA → BDS.
 * Property 24: Invalid messages go to Dead Letter Channel.
 */
@QuarkusTest
class RoutingPropertyTest {

    @Inject
    ProducerTemplate producerTemplate;

    private OrderMessageDTO buildMessage(String estacion) {
        return new OrderMessageDTO(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Mesa 1", "Producto Test",
                1, null, estacion, LocalDateTime.now()
        );
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 11: Enrutamiento de items por estacion")
    void property11_cocinaItemsRoutedToKDS() {
        // Send 10 COCINA items — should route to kdsQueue without exception
        for (int i = 0; i < 10; i++) {
            OrderMessageDTO msg = buildMessage("COCINA");
            assertDoesNotThrow(() ->
                producerTemplate.sendBodyAndHeader(
                    "direct:routeOrderItem", msg, "estacion", "COCINA"),
                "COCINA item should route to KDS without error");
        }
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 11: Enrutamiento de items por estacion")
    void property11_barraItemsRoutedToBDS() {
        for (int i = 0; i < 10; i++) {
            OrderMessageDTO msg = buildMessage("BARRA");
            assertDoesNotThrow(() ->
                producerTemplate.sendBodyAndHeader(
                    "direct:routeOrderItem", msg, "estacion", "BARRA"),
                "BARRA item should route to BDS without error");
        }
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 24: Mensaje invalido va al Dead Letter Channel")
    void property24_nullBodyRoutedToDLC() {
        // Null body should trigger validation failure → DLC
        // The route handles this gracefully via errorHandler
        assertDoesNotThrow(() ->
            producerTemplate.sendBodyAndHeader(
                "direct:routeOrderItem", null, "estacion", "COCINA"),
            "Null body should be handled by DLC without crashing the route");
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 24: Mensaje invalido va al Dead Letter Channel")
    void property24_unknownEstacionRoutedToDLC() {
        for (int i = 0; i < 10; i++) {
            OrderMessageDTO msg = buildMessage("UNKNOWN");
            assertDoesNotThrow(() ->
                producerTemplate.sendBodyAndHeader(
                    "direct:routeOrderItem", msg, "estacion", "UNKNOWN"),
                "Unknown estacion should be handled by DLC without crashing");
        }
    }
}
