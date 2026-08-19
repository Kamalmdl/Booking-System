package org.kamal.paymentservice.event;

public record PaymentResultEvent(Long bookingId, boolean success) {
}
