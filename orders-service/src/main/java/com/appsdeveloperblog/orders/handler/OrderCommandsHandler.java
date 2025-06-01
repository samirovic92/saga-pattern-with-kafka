package com.appsdeveloperblog.orders.handler;

import com.appsdeveloperblog.core.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.commands.RejectOrderCommand;
import com.appsdeveloperblog.orders.service.OrderService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${kafka.topic.order.commands.name}")
public class OrderCommandsHandler {
    private final OrderService orderService;

    public OrderCommandsHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaHandler
    public void handleCommand(@Payload ApproveOrderCommand command) {
        orderService.approveOrder(command.orderId());
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectOrderCommand command) {
        orderService.rejectOrder(command.orderId());
    }
}
