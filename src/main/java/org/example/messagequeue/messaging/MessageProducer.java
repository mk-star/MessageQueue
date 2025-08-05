package org.example.messagequeue.messaging;

import jakarta.transaction.Transactional;
import org.example.messagequeue.config.RabbitMQConfig;
import org.example.messagequeue.entity.StockEntity;
import org.example.messagequeue.repository.StockRepository;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;
    private final StockRepository stockRepository;

    public MessageProducer(RabbitTemplate rabbitTemplate, StockRepository stockRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void sendMessage(StockEntity stockEntity, boolean testCase) {
        stockEntity.setProcessed(false);
        stockEntity.setCreatedAt(LocalDateTime.now());
        StockEntity entity = stockRepository.save(stockEntity);

        System.out.println("[producer entity] : " + entity);

        // 저장했는데 유저 아이디가 없는 경우 롤백
        if (stockEntity.getUserId() == null || stockEntity.getUserId().isEmpty()) {
            throw new RuntimeException("User id is required");
        }

        try {
            // 메시지를 rabbitmq에 전송
            // CorrelationDataㅇ은 퍼블리셔 컨펌에서 사용되는 객체로 메시지 전송 상태를 추적하기 위해 사용됨
            // 성공, 실패에 따라 추가 로직을 작성하기 위해

            // 메시지 추적 id : 파라미터로 온 값. 걔를 트레킹하는 용도
            CorrelationData correlationData = new CorrelationData(entity.getId().toString());
            rabbitTemplate.convertAndSend(
                    // true면 존재하지 않는 exchange로 보내고 아니면 false면 기존 exchange
                    testCase ? "nonExistentExchange" : RabbitMQConfig.EXCHANGE_NAME,
                    testCase ? "invalidRoutingKey" : RabbitMQConfig.ROUTING_KEY,
                    entity,
                    correlationData
            );

            // 5초 안에 데이터가 들어오면 메시지 전송이 된 거니까(컨펌이 된 것) 엔티티 상태 변경
            // ack/nack에 따라서 다르게 처리
            if (correlationData.getFuture().get(5, TimeUnit.SECONDS).isAck()) {
                System.out.println("[producer correlationData] 성공" + entity);
                entity.setProcessed(true);
                stockRepository.save(entity);
            } else {
                throw new RuntimeException("# confirm 실패 - 롤백");
            }

        } catch (Exception e) {
            System.out.println("[producer exception fail] : " + e);
            throw new RuntimeException(e);
        }
    }
}