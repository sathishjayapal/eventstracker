package me.sathish.event_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.util.ApplicationProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfiguration {

    public static final String GITHUB_QUEUE = "x.github.operations";
    public static final String EVENTS_EXCHANGE = "x.sathishprojects.events";
    public static final String GITHUB_ROUTING_KEY = "github.operations.crud";
    public static final String GARMIN_QUEUE = "x.garmin.operations";
    public static final String GARMIN_ROUTING_KEY = "garmin.operations.crud";
    private final ApplicationProperties applicationProperties;

    public RabbitMQConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public Queue githubQueue() {
        log.info("Creating RabbitMQ Queue: {}", GITHUB_QUEUE);
        return new Queue(GITHUB_QUEUE, true);
    }

    @Bean
    public Queue garminQueue() {
        log.info("Creating RabbitMQ Queue: {}", GARMIN_QUEUE);
        return new Queue(GARMIN_QUEUE, true);
    }

    @Bean
    public TopicExchange eventsExchange() {
        log.info("Creating RabbitMQ Exchange: {}", EVENTS_EXCHANGE);
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public Binding githubBinding(Queue githubQueue, TopicExchange eventsExchange) {
        log.info("Creating RabbitMQ Binding: Queue={}, Exchange={}, RoutingKey={}",
            GITHUB_QUEUE, EVENTS_EXCHANGE, GITHUB_ROUTING_KEY);
        return BindingBuilder.bind(githubQueue).to(eventsExchange).with(GITHUB_ROUTING_KEY);
    }

    @Bean
    public Binding garminBinding(Queue garminQueue, TopicExchange eventsExchange) {
        log.info("Creating RabbitMQ Binding: Queue={}, Exchange={}, RoutingKey={}",
            GARMIN_QUEUE, EVENTS_EXCHANGE, GARMIN_ROUTING_KEY);
        return BindingBuilder.bind(garminQueue).to(eventsExchange).with(GARMIN_ROUTING_KEY);
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
