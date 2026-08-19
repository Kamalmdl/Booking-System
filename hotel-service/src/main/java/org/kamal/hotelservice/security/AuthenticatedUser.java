package org.kamal.hotelservice.security;

public record AuthenticatedUser (Long userId, String email){
}
