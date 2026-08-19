package org.kamal.bookingservice.event;

public record PaymentResultEvent(Long bookingId, Long userId, boolean success) {
}
