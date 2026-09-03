package org.kamal.bookingservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kamal.bookingservice.client.RoomClient;
import org.kamal.bookingservice.dto.request.BookingRequest;
import org.kamal.bookingservice.dto.response.BookingResponse;
import org.kamal.bookingservice.dto.response.RoomResponse;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.exception.InvalidBookingDatesException;
import org.kamal.bookingservice.exception.RoomNotAvailableException;
import org.kamal.bookingservice.exception.RoomNotFoundException;
import org.kamal.bookingservice.repository.BookingRepository;
import org.kamal.bookingservice.security.AuthenticatedUser;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingTransactionServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomClient roomClient;

    @InjectMocks
    private BookingTransactionService bookingTransactionService;

    @BeforeEach
    public void setup() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(1L, "kamal@gmail.com");

        Authentication  authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSaveCorrectBookingFields_whenBookingRequestIsValid() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,1), LocalDate.of(2026,1,4));
        RoomResponse roomResponse = new RoomResponse(1L, new BigDecimal("100"));

        when(roomClient.getRoomById(bookingRequest.getRoomId())).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())).thenReturn(List.of());

        BookingResponse result = bookingTransactionService.saveBookingWithLock(bookingRequest);

        assertEquals(bookingRequest.getRoomId(), result.getRoomId());
        assertEquals(bookingRequest.getCheckInDate(), result.getCheckInDate());
        assertEquals(bookingRequest.getCheckOutDate(), result.getCheckOutDate());
        assertEquals(new BigDecimal("300"), result.getPrice());
    }

    @Test
    void shouldThrowInvalidBookingDatesException_whenCheckOutDateIsBeforeCheckInDate() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,4), LocalDate.of(2026,1,2));

        assertThrows(InvalidBookingDatesException.class, ()  -> bookingTransactionService.saveBookingWithLock(bookingRequest));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowRoomNotFoundException_whenRoomNotFound() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,1), LocalDate.of(2026,1,4));
        HttpClientErrorException notFoundException = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Room not found", HttpHeaders.EMPTY, null, null);
        when(roomClient.getRoomById(bookingRequest.getRoomId())).thenThrow(notFoundException);

        assertThrows(RoomNotFoundException.class, () -> bookingTransactionService.saveBookingWithLock(bookingRequest));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowRoomNotAvailableException_whenRoomNotAvailable() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,1), LocalDate.of(2026,1,4));
        RoomResponse roomResponse = new RoomResponse(1L, new BigDecimal("100"));

        when(roomClient.getRoomById(bookingRequest.getRoomId())).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())).thenReturn(List.of(new Booking()));

        assertThrows(RoomNotAvailableException.class, () -> bookingTransactionService.saveBookingWithLock(bookingRequest));

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
