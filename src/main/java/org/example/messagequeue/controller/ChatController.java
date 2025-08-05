package org.example.messagequeue.controller;

import lombok.RequiredArgsConstructor;
import org.example.messagequeue.dto.ChatMessage;
import org.example.messagequeue.messaging.ChatProducer;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatProducer chatProducer;

    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage message) {
        System.out.println("[#] roomId = " + roomId);
        System.out.println("[#] message = " + message.message());
        chatProducer.sendMessage(message);
    }
}