package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${kafka.topic.order.events.name}")
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productTopicName;
    private final OrderHistoryService orderHistoryService;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     @Value("${kafka.topic.product.commands.name}") String productTopicName,
                     OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.productTopicName = productTopicName;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {
        var reserveProductCommand = new ReserveProductCommand(
                event.orderId(),
                event.productId(),
                event.productQuantity()
        );
        this.kafkaTemplate.send(productTopicName, reserveProductCommand);
        this.orderHistoryService.add(event.orderId(), OrderStatus.CREATED);
    }
}
