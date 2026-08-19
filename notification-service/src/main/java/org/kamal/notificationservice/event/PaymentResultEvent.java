package org.kamal.notificationservice.event;

public record PaymentResultEvent(Long bookingId, boolean success){
}
