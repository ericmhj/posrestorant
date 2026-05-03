package com.restaurant.pos.camel;

import com.restaurant.pos.websocket.WebSocketBroadcastService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class OrderRoutingRoute extends RouteBuilder {

    @Inject
    WebSocketBroadcastService broadcastService;

    @Inject
    DeadLetterService deadLetterService;

    @Override
    public void configure() {

        // Dead Letter Channel: 3 retries with 1s delay, then DLC
        errorHandler(deadLetterChannel("direct:deadLetter")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000)
                .logExhausted(true)
                .logRetryAttempted(true));

        // -------------------------------------------------------
        // Route 1: Content-Based Router — routes items to KDS or BDS
        // -------------------------------------------------------
        from("direct:routeOrderItem")
                .routeId("order-routing-route")
                .log(LoggingLevel.INFO, "Routing OrderItem: itemPedidoId=${header.itemPedidoId} estacion=${header.estacion}")
                .validate(body().isNotNull())
                .choice()
                    .when(header("estacion").isEqualTo("COCINA"))
                        .to("direct:kdsQueue")
                    .when(header("estacion").isEqualTo("BARRA"))
                        .to("direct:bdsQueue")
                    .otherwise()
                        .log(LoggingLevel.WARN, "Unknown estacion: ${header.estacion}, routing to DLC")
                        .to("direct:deadLetter")
                .end()
                .to("direct:metricsCollector");

        // -------------------------------------------------------
        // Route 2: KDS notification
        // -------------------------------------------------------
        from("direct:kdsQueue")
                .routeId("kds-notification-route")
                .log(LoggingLevel.INFO, "Notifying KDS: mesa=${body.mesaNombre} producto=${body.productoNombre}")
                .process(exchange -> {
                    OrderMessageDTO msg = exchange.getIn().getBody(OrderMessageDTO.class);
                    broadcastService.broadcastToKDS(msg);
                })
                .to("direct:metricsCollector");

        // -------------------------------------------------------
        // Route 3: BDS notification
        // -------------------------------------------------------
        from("direct:bdsQueue")
                .routeId("bds-notification-route")
                .log(LoggingLevel.INFO, "Notifying BDS: mesa=${body.mesaNombre} producto=${body.productoNombre}")
                .process(exchange -> {
                    OrderMessageDTO msg = exchange.getIn().getBody(OrderMessageDTO.class);
                    broadcastService.broadcastToBDS(msg);
                })
                .to("direct:metricsCollector");

        // -------------------------------------------------------
        // Route 4: Dead Letter Channel
        // -------------------------------------------------------
        from("direct:deadLetter")
                .routeId("dead-letter-route")
                .log(LoggingLevel.ERROR, "Message failed after retries: ${body}")
                .bean(deadLetterService, "persist");

        // -------------------------------------------------------
        // Route 5: Metrics collector
        // -------------------------------------------------------
        from("direct:metricsCollector")
                .routeId("metrics-route")
                .log(LoggingLevel.DEBUG, "Metrics: route=${header.CamelToEndpoint}");
    }
}
