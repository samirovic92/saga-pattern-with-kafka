package com.appsdeveloperblog.payments.handler;

import com.appsdeveloperblog.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.Payment;
import com.appsdeveloperblog.core.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.payments.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${kafka.topic.payment.commands.name}")
public class PaymentCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCommandHandler.class);
    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String paymentEventTopicName;

    public PaymentCommandHandler(PaymentService paymentService,
                                 KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${kafka.topic.payment.events.name}") String paymentEventTopicName) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentEventTopicName = paymentEventTopicName;
    }

    @KafkaHandler
    public void handle(ProcessPaymentCommand command) {
        try {
            var payment = new Payment(
                    command.orderId(),
                    command.productId(),
                    command.productPrice(),
                    command.productQuantity()
            );
            var processedPayment = this.paymentService.process(payment);
            var paymentProcessedEvent = new PaymentProcessedEvent(
                    processedPayment.getOrderId(),
                    processedPayment.getId()
            );
            kafkaTemplate.send(paymentEventTopicName, paymentProcessedEvent);
        } catch (Exception e) {
            logger.error("Error process payment: {}", e.getMessage());
            var paymentFailedEvent = new PaymentFailedEvent(
                    command.orderId(),
                    command.productId(),
                    command.productQuantity()
            );
            kafkaTemplate.send(paymentEventTopicName, paymentFailedEvent);
        }

    }
}
