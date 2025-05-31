package com.appsdeveloperblog.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic ordersTopic(@Value("${kafka.topic.order.events.name}") String ordersEventsTopicName) {
        return TopicBuilder
                .name(ordersEventsTopicName)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic productsTopic(@Value("${kafka.topic.product.commands.name}") String productsEventsTopicName) {
        return TopicBuilder
                .name(productsEventsTopicName)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
