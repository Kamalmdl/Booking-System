package org.kamal.notificationservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics="payment-results", groupId = "notification-service")
    public void handlePaymentResult(PaymentResultEvent event) {
        if(event.success()) {
            log.info("Booking #{} confirmed. Sending confirmation notification to user.", event.bookingId());
        }
        else {
            log.warn("Payment failed for booking #{}. Sending failure notification to user.", event.bookingId());
        }
    }
}
