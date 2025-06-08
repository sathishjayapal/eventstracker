package me.sathish.event_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.sathish.event_service.util.ApplicationProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    private final ApplicationProperties applicationProperties;

    public RabbitMQConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    DirectExchange garminRunExchange() {
        return new DirectExchange(applicationProperties.garminExchange());
    }

    @Bean
    Queue newGarminRunQueue() {
        return new Queue(applicationProperties.garminNewRunQueue());
    }

    @Bean
    Binding newGarminRunBinding() {
        return BindingBuilder.bind(newGarminRunQueue())
                .to(garminRunExchange())
                .with(applicationProperties.garminNewRunQueue());
    }

    @Bean
    Queue errorGarminRunQueue() {
        return new Queue(applicationProperties.garminErrorQueue());
    }

    @Bean
    Binding errorGarminRunBinding() {
        return BindingBuilder.bind(errorGarminRunQueue())
                .to(garminRunExchange())
                .with(applicationProperties.garminErrorQueue());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonConverter(objectMapper));
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
