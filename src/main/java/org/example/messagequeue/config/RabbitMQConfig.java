package org.example.messagequeue.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "notificationQueue";
    public static final String FANOUT_EXCHANGE = "notificationExchange";

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        // 메시지를 수신하면 연결된 모든 큐로 브로드캐스트
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding bindNotification(Queue notificationQueue, FanoutExchange fanoutExchange) {
        // BindingBuilder.bind.to() 를 통해 Queue와 Exchange를 연결
        return BindingBuilder.bind(notificationQueue).to(fanoutExchange);
    }
}
