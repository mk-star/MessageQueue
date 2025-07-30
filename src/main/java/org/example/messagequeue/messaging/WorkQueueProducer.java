package org.example.messagequeue.messaging;

import org.example.messagequeue.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkQueueProducer {
    private final RabbitTemplate rabbitTemplate;

    public WorkQueueProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendWorkQueue(String workQueueMessage, int duration) {
        String message = workQueueMessage + "|" + duration;
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        System.out.println("[#] Sent workqueue " + message);
    }
}