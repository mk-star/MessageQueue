package org.example.messagequeue.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "transactionQueue";
    public static final String EXCHANGE_NAME = "transactionExchange";
    public static final String ROUTING_KEY = "transactionRoutingKey";

    // Queue 설정
    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "") // Dead Letter Exchange
                .withArgument("x-dead-letter-routing-key", "deadLetterQueue") // Dead Letter Routing Key
                .build();
    }

    // Dead Letter Queue 설정
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("deadLetterQueue");
    }

    // Exchange 설정
    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // Binding 설정
    @Bean
    public Binding transactionBinding(Queue transactionQueue, DirectExchange transactionExchange) {
        return BindingBuilder.bind(transactionQueue).to(transactionExchange).with(ROUTING_KEY);
    }

    // 메시지 변환기 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate 설정, ReturnsCallback 활성화 등록, ConfirmCallback 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter()); // JSON 변환기
        rabbitTemplate.setMandatory(true);  // ReturnCallback 활성화

        // confirmCallBack 설정
        // 메시지가 exchange에 잘 도착했느냐..를 확인하는 callback
        // 컨펌이 떨어져야 실제 트랜잭션 처리를 함
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                //  메시지가 exchange에 도달하였다.
                System.out.println("#### [Message confirmed]: " +
                        (correlationData != null ? correlationData.getId() : "null"));
            } else {
                // 메시지가 exchange에 도달하지 못했다
                System.out.println("#### [Message not confirmed]: " +
                        (correlationData != null ? correlationData.getId() : "null") + ", Reason: " + cause);

                //재처리, DLQ에 넣다던지..
                // 실패 메시지에 대한 추가 처리 로직 (예: 로그 기록, DB 적재, 관리자 알림 등)
            }
        });

        // ReturnCallback 설정
        // setMandatory(true)로 해서 리턴 콜백이 활성화가 됨
        // exchange에서 queue로 라우팅하지 못했을 경우 메시지 확인
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("Return Message: " + returned.getMessage().getBody());
            System.out.println("Exchange : " + returned.getExchange());
            System.out.println("RoutingKey : " + returned.getRoutingKey());

            // 데드레터 설정 추가
        });
        return rabbitTemplate;
    }


    // RabbitListener 설정, 수동 Ack 모드 설정
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 Ack 모드
        return factory;
    }

}