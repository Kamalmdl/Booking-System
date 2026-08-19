package org.kamal.bookingservice.event;

import java.math.BigDecimal;

public record PaymentRequestEvent(Long bookingId, Long userId, BigDecimal amount) {
}
