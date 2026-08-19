package org.kamal.paymentservice.event;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultProducer {

    private final KafkaTemplate<String, PaymentResultEvent> kafkaTemplate;

    private static final String TOPIC = "payment-results";

    public void sendPaymentResult(PaymentResultEvent event) {
        kafkaTemplate.send(TOPIC, event.bookingId().toString(), event);
    }
}
