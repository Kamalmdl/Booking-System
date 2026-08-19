package org.kamal.bookingservice.security;

public record AuthenticatedUser (Long userId, String email){
}
