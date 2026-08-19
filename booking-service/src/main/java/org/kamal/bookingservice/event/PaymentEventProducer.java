package org.kamal.bookingservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

    private static final String TOPIC = "payment-requests";

    public void sendPaymentRequest(PaymentRequestEvent event) {
        kafkaTemplate.send(TOPIC, event.bookingId().toString(), event);
    }
}
