package org.kamal.paymentservice.event;

import lombok.RequiredArgsConstructor;
import org.kamal.paymentservice.entity.Payment;
import org.kamal.paymentservice.entity.PaymentStatus;
import org.kamal.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentResultProducer paymentResultProducer;


    @KafkaListener(topics = "payment-requests",groupId = "payment-service")
    public void handlePaymentRequest(PaymentRequestEvent paymentRequestEvent) {
        Payment payment = new Payment();
        payment.setBookingId(paymentRequestEvent.bookingId());
        payment.setUserId(paymentRequestEvent.userId());
        payment.setAmount(paymentRequestEvent.amount());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        boolean success = payment.getPaymentStatus()==PaymentStatus.SUCCESS;
        PaymentResultEvent paymentResultEvent = new PaymentResultEvent(
                payment.getBookingId(),
                success
        );

        paymentResultProducer.sendPaymentResult(paymentResultEvent);
    }
}
