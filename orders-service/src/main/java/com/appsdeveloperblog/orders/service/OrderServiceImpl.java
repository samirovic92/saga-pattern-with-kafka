package com.appsdeveloperblog.orders.service;

import com.appsdeveloperblog.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.dto.Order;
import com.appsdeveloperblog.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.dao.jpa.entity.OrderEntity;
import com.appsdeveloperblog.orders.dao.jpa.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersEventsTopicName;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topic.order.events.name}") String ordersEventsTopicName
    ) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersEventsTopicName = ordersEventsTopicName;
    }

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);
        var orderCreatedEvent = new OrderCreatedEvent(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity()
        );
        kafkaTemplate.send(ordersEventsTopicName, orderCreatedEvent);

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    public void approveOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(entity, "Order does not exist");
        entity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(entity);
        var orderCreatedEvent = new OrderApprovedEvent(orderId);
        kafkaTemplate.send(ordersEventsTopicName, orderCreatedEvent);
    }

    @Override
    public void rejectOrder(UUID uuid) {
        OrderEntity entity = orderRepository.findById(uuid).orElse(null);
        Assert.notNull(entity, "Order does not exist");
        entity.setStatus(OrderStatus.REJECTED);
        orderRepository.save(entity);
        var orderRejectedEvent = new OrderRejectedEvent(uuid);
        kafkaTemplate.send(ordersEventsTopicName, orderRejectedEvent);
    }
}
