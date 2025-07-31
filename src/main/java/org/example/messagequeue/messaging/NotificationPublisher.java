package org.example.messagequeue.messaging;

import org.example.messagequeue.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisher {
    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", message); // Fanout에서 routing key는 무시됨
        System.out.println("[#] Published Notification: " + message);
    }

//    public void convertAndSend(String exchange, String routingKey, Object object) throws AmqpException {
//        this.convertAndSend(exchange, routingKey, object, (CorrelationData)null);
//    }
}
