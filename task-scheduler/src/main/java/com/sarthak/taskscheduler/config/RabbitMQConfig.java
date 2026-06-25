package com.sarthak.taskscheduler.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "task_exchange";
    public static final String QUEUE_NAME = "task_queue";
    public static final String DLQ_NAME = "task_dlq";

    public static final String ROUTING_KEY = "task.routing";
    public static final String DLQ_ROUTING_KEY = "task.dlq.routing";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue taskQueue() {
        Map<String, Object> args = new HashMap<>();

        args.put(
                "x-dead-letter-exchange",
                EXCHANGE_NAME
        );

        args.put(
                "x-dead-letter-routing-key",
                DLQ_ROUTING_KEY
        );

        return new Queue(
                QUEUE_NAME,
                true,
                false,
                false,
                args
        );
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(
                DLQ_NAME,
                true
        );
    }

    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(taskQueue())
                .to(exchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(exchange())
                .with(DLQ_ROUTING_KEY);
    }
}