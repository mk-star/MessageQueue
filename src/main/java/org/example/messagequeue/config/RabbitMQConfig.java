package org.example.messagequeue.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "chat.queue";
    public static final String TOPIC_EXCHANGE = "chat.exchange";
    public static final String ROUTING_KEY = "chat.room.*";

    @Bean
    public Queue chatQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        // 메시지를 수신하면 해당 토픽을 구독하는
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding bindNotification(Queue chatQueue, TopicExchange topicExchange) {
        // BindingBuilder.bind().to() 를 통해 큐와 익스체인지를 연결
        return BindingBuilder.bind(chatQueue).to(topicExchange).with(ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter); // JSON 변환기 등록
        return rabbitTemplate;
    }

    // 메시지 송수신 시 메시지 바디를 JSON으로 직렬화/역직렬화
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}