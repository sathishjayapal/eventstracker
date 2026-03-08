package me.sathish.event_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitSchemaConfig {

    // Exchange names
    public static final String FANOUT_EXCHANGE = "x.sathishprojects.fanout";
    public static final String DLX_EXCHANGE = "x.sathishprojects.dlx.exchange";
    public static final String GITHUB_EVENTS_EXCHANGE = "x.sathishprojects.github.events.exchange";
    public static final String GITHUB_EVENTS_DLX_EXCHANGE = "x.sathishprojects.github.events.dlx.exchange";
    public static final String GARMIN_EVENTS_EXCHANGE = "x.sathishprojects.garmin.events.exchange";
    public static final String GARMIN_EVENTS_DLX_EXCHANGE = "x.sathishprojects.garmin.events.dlx.exchange";

    // Queue names
    public static final String SAT_PROJECTS_EVENTS_QUEUE = "q.sathishprojects.events";
    public static final String DLQ_SAT_PROJECTS_EVENTS_QUEUE = "dlq.sathishprojects.events";
    public static final String GITHUB_API_EVENTS_QUEUE = "q.sathishprojects.github.api.events";
    public static final String GITHUB_OPS_EVENTS_QUEUE = "q.sathishprojects.github.ops.events";
    public static final String DLQ_GITHUB_API_EVENTS_QUEUE = "dlq.sathishprojects.github.api.events";
    public static final String DLQ_GITHUB_OPS_EVENTS_QUEUE = "dlq.sathishprojects.github.ops.events";
    public static final String GARMIN_API_EVENTS_QUEUE = "q.sathishprojects.garmin.api.events";
    public static final String GARMIN_OPS_EVENTS_QUEUE = "q.sathishprojects.garmin.ops.events";
    public static final String DLQ_GARMIN_API_EVENTS_QUEUE = "dlq.sathishprojects.garmin.api.events";
    public static final String DLQ_GARMIN_OPS_EVENTS_QUEUE = "dlq.sathishprojects.garmin.ops.events";

    // Routing key patterns (used for queue bindings)
    public static final String GITHUB_API_ROUTING_KEY = "sathishprojects.github.api.*";
    public static final String GITHUB_OPS_ROUTING_KEY = "sathishprojects.github.ops.*";
    public static final String GARMIN_API_ROUTING_KEY = "sathishprojects.garmin.api.*";
    public static final String GARMIN_OPS_ROUTING_KEY = "sathishprojects.garmin.ops.*";

    public static final int MESSAGE_TTL_MS = 10000;

    @Bean
    public Declarables declarables() {
        // Exchanges
        FanoutExchange fanoutExchange = new FanoutExchange(FANOUT_EXCHANGE);
        TopicExchange dlxExchange = new TopicExchange(DLX_EXCHANGE);
        TopicExchange gitHubEventsExchange = new TopicExchange(GITHUB_EVENTS_EXCHANGE);
        TopicExchange gitHubEventsDlxExchange = new TopicExchange(GITHUB_EVENTS_DLX_EXCHANGE);
        TopicExchange garminEventsExchange = new TopicExchange(GARMIN_EVENTS_EXCHANGE);
        TopicExchange garminEventsDlxExchange = new TopicExchange(GARMIN_EVENTS_DLX_EXCHANGE);

        // Catch-all queue (no TTL — used for audit/replay)
        Queue satProjectsEventsQueue = QueueBuilder.durable(SAT_PROJECTS_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .build();
        Queue dlqSatProjectsEventsQueue = QueueBuilder.durable(DLQ_SAT_PROJECTS_EVENTS_QUEUE).build();

        // GitHub domain queues
        Queue gitHubApiEventsQueue = QueueBuilder.durable(GITHUB_API_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", GITHUB_EVENTS_DLX_EXCHANGE)
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .withArgument("x-dead-letter-routing-key", GITHUB_API_ROUTING_KEY)
                .build();
        Queue gitHubOpsEventsQueue = QueueBuilder.durable(GITHUB_OPS_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", GITHUB_EVENTS_DLX_EXCHANGE)
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .withArgument("x-dead-letter-routing-key", GITHUB_OPS_ROUTING_KEY)
                .build();
        Queue dlqGitHubApiEventsQueue = QueueBuilder.durable(DLQ_GITHUB_API_EVENTS_QUEUE).build();
        Queue dlqGitHubOpsEventsQueue = QueueBuilder.durable(DLQ_GITHUB_OPS_EVENTS_QUEUE).build();

        // Garmin domain queues
        Queue garminApiEventsQueue = QueueBuilder.durable(GARMIN_API_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", GARMIN_EVENTS_DLX_EXCHANGE)
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .withArgument("x-dead-letter-routing-key", GARMIN_API_ROUTING_KEY)
                .build();
        Queue garminOpsEventsQueue = QueueBuilder.durable(GARMIN_OPS_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", GARMIN_EVENTS_DLX_EXCHANGE)
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .withArgument("x-dead-letter-routing-key", GARMIN_OPS_ROUTING_KEY)
                .build();
        Queue dlqGarminApiEventsQueue = QueueBuilder.durable(DLQ_GARMIN_API_EVENTS_QUEUE).build();
        Queue dlqGarminOpsEventsQueue = QueueBuilder.durable(DLQ_GARMIN_OPS_EVENTS_QUEUE).build();

        return new Declarables(
                // Exchanges
                fanoutExchange,
                dlxExchange,
                gitHubEventsExchange,
                gitHubEventsDlxExchange,
                garminEventsExchange,
                garminEventsDlxExchange,

                // Queues
                satProjectsEventsQueue,
                dlqSatProjectsEventsQueue,
                gitHubApiEventsQueue,
                gitHubOpsEventsQueue,
                dlqGitHubApiEventsQueue,
                dlqGitHubOpsEventsQueue,
                garminApiEventsQueue,
                garminOpsEventsQueue,
                dlqGarminApiEventsQueue,
                dlqGarminOpsEventsQueue,

                // Bindings: catch-all → fanout
                BindingBuilder.bind(satProjectsEventsQueue).to(fanoutExchange),
                BindingBuilder.bind(dlqSatProjectsEventsQueue).to(dlxExchange).with("#"),

                // Bindings: GitHub exchange → topic queues
                BindingBuilder.bind(gitHubApiEventsQueue).to(gitHubEventsExchange).with(GITHUB_API_ROUTING_KEY),
                BindingBuilder.bind(gitHubOpsEventsQueue).to(gitHubEventsExchange).with(GITHUB_OPS_ROUTING_KEY),
                BindingBuilder.bind(dlqGitHubApiEventsQueue).to(gitHubEventsDlxExchange).with(GITHUB_API_ROUTING_KEY),
                BindingBuilder.bind(dlqGitHubOpsEventsQueue).to(gitHubEventsDlxExchange).with(GITHUB_OPS_ROUTING_KEY),

                // Bindings: Garmin exchange → topic queues
                BindingBuilder.bind(garminApiEventsQueue).to(garminEventsExchange).with(GARMIN_API_ROUTING_KEY),
                BindingBuilder.bind(garminOpsEventsQueue).to(garminEventsExchange).with(GARMIN_OPS_ROUTING_KEY),
                BindingBuilder.bind(dlqGarminApiEventsQueue).to(garminEventsDlxExchange).with(GARMIN_API_ROUTING_KEY),
                BindingBuilder.bind(dlqGarminOpsEventsQueue).to(garminEventsDlxExchange).with(GARMIN_OPS_ROUTING_KEY),

                // Exchange-to-exchange bindings: domain exchanges → fanout
                BindingBuilder.bind(gitHubEventsExchange).to(fanoutExchange),
                BindingBuilder.bind(garminEventsExchange).to(fanoutExchange));
    }
}
