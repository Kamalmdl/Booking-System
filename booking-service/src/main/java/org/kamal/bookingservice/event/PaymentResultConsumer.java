package org.kamal.bookingservice.event;

import lombok.RequiredArgsConstructor;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.entity.BookingStatus;
import org.kamal.bookingservice.exception.BookingNotFoundException;
import org.kamal.bookingservice.repository.BookingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "payment-results", groupId = "booking-service")
    public void handlePaymentResult(PaymentResultEvent event) {
        Booking booking = bookingRepository.findById(event.bookingId()).orElseThrow(()-> new BookingNotFoundException("Booking was not found"));
        if(!event.success()) {
            booking.setStatus(BookingStatus.PAYMENT_FAILED);
        } else {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);
    }
}
