package com.example.calculatorapi.config;

import com.example.calculatorapi.dto.CalculationRequest;
import com.example.calculatorapi.dto.CalculationResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.group.rest}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, CalculationResponse> consumerFactory() {
        JsonDeserializer<CalculationResponse> deserializer =
            new JsonDeserializer<>(CalculationResponse.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        );

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CalculationResponse> restKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, CalculationResponse>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, CalculationRequest> producerFactory() {
        Map<String, Object> props = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, CalculationRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
} 