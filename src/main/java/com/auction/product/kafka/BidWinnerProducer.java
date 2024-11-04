package com.auction.product.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidWinnerProducer {

    private final KafkaTemplate<String, BidWinnerConfirmation> kafkaTemplate;
    public void sendBidWinnerConfirmation(BidWinnerConfirmation bidWinnerConfirmation) {
        log.info("Sending Bid winner confirmation");
        Message<BidWinnerConfirmation> message = MessageBuilder
                .withPayload(bidWinnerConfirmation)
                .setHeader(KafkaHeaders.TOPIC, "bid-topic")
                .build();
        kafkaTemplate.send(message);
    }
}
