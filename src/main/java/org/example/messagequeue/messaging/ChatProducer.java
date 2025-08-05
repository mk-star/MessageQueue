package org.example.messagequeue.messaging;

import lombok.RequiredArgsConstructor;
import org.example.messagequeue.config.RabbitMQConfig;
import org.example.messagequeue.dto.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(ChatMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, "chat.room." + message.roomId(), message);
        System.out.println("[#] Published message: " + message);
    }
}