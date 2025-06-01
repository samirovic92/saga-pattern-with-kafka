package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.commands.RejectOrderCommand;
import com.appsdeveloperblog.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.core.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.core.events.ProductReservedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${kafka.topic.order.events.name}",
        "${kafka.topic.product.events.name}",
        "${kafka.topic.payment.events.name}"
})
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;
    private final String productTopicName;
    private final String paymentTopicName;
    private final String orderTopicName;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     OrderHistoryService orderHistoryService,
                     @Value("${kafka.topic.product.commands.name}") String productTopicName,
                     @Value("${kafka.topic.payment.commands.name}") String paymentTopicName,
                     @Value("${kafka.topic.order.commands.name}") String orderTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.productTopicName = productTopicName;
        this.orderHistoryService = orderHistoryService;
        this.paymentTopicName = paymentTopicName;
        this.orderTopicName = orderTopicName;
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

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event) {
        var processPaymentCommand = new ProcessPaymentCommand(
                event.orderId(),
                event.productId(),
                event.productQuantity(),
                event.productPrice()
        );
        kafkaTemplate.send(paymentTopicName, processPaymentCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentProcessedEvent event) {
        var ApproveOrderCommand = new ApproveOrderCommand(
                event.orderId()
        );
        kafkaTemplate.send(orderTopicName, ApproveOrderCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderApprovedEvent event) {
        this.orderHistoryService.add(event.orderId(), OrderStatus.APPROVED);
    }

    // Compensating Event Handlers

    @KafkaHandler
    public void handleEvent(@Payload PaymentFailedEvent event) {
        var cancelProductReservationCommand = new CancelProductReservationCommand(
                event.orderId(),
                event.productId(),
                event.productQuantity()
        );
        kafkaTemplate.send(productTopicName, cancelProductReservationCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservationCancelledEvent event) {
        var rejectOrderCommand = new RejectOrderCommand(event.orderId());
        kafkaTemplate.send(orderTopicName, rejectOrderCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderRejectedEvent event) {
        this.orderHistoryService.add(event.orderId(), OrderStatus.REJECTED);
    }
}
