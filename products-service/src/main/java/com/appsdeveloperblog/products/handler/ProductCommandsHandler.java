package com.appsdeveloperblog.products.handler;

import com.appsdeveloperblog.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.core.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.events.ProductReservedEvent;
import com.appsdeveloperblog.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${kafka.topic.product.commands.name}")
public class ProductCommandsHandler {
    private static final Logger log = LoggerFactory.getLogger(ProductCommandsHandler.class);
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productEventsTopicName;

    public ProductCommandsHandler(ProductService productService,
                                  @Value("${kafka.topic.product.events.name}") String productEventsTopicName,
                                  KafkaTemplate<String, Object> kafkaTemplate) {
        this.productService = productService;
        this.productEventsTopicName = productEventsTopicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handle(@Payload ReserveProductCommand command) {
        try {
            var desiredProduct = new Product(command.productId(), command.productQuantity());
            var reservedProduct = productService.reserve(desiredProduct, command.orderId());
            var productReservedEvent = new ProductReservedEvent(
                    command.orderId(),
                    command.productId(),
                    command.productQuantity(),
                    reservedProduct.getPrice()
            );
            kafkaTemplate.send(productEventsTopicName, productReservedEvent);
        } catch (Exception e) {
            log.error("Error reserving product: {}", e.getMessage());
            var productReservationFailedEvent = new ProductReservationFailedEvent(
                    command.orderId(),
                    command.productId(),
                    command.productQuantity()
            );
            kafkaTemplate.send(productEventsTopicName, productReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handle(@Payload CancelProductReservationCommand command) {
        var productToCancel = new Product(command.productId(), command.productQuantity());
        productService.cancelReservation(productToCancel, command.orderId());
        var productReservationCancelledEvent = new ProductReservationCancelledEvent(
                command.orderId(),
                command.productId()
        );
        kafkaTemplate.send(productEventsTopicName, productReservationCancelledEvent);
    }
}
