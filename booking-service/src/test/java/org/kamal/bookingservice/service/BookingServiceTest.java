package org.kamal.bookingservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kamal.bookingservice.client.UserClient;
import org.kamal.bookingservice.dto.request.BookingRequest;
import org.kamal.bookingservice.dto.response.BookingResponse;
import org.kamal.bookingservice.dto.response.UserResponse;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.entity.BookingStatus;
import org.kamal.bookingservice.event.PaymentEventProducer;
import org.kamal.bookingservice.event.PaymentRequestEvent;
import org.kamal.bookingservice.exception.BookingNotFoundException;
import org.kamal.bookingservice.exception.InvalidBookingDatesException;
import org.kamal.bookingservice.repository.BookingRepository;
import org.kamal.bookingservice.security.AuthenticatedUser;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentEventProducer paymentRequestProducer;

    @Mock
    private BookingTransactionService bookingTransactionService;

    @Mock
    private UserClient  userClient;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    public void setup() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(1L, "kamal@gmail.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldCreateBookingAndSendPaymentRequest_whenBookingIsValid() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,1), LocalDate.of(2026,1,4));
        BookingResponse bookingResponse = new BookingResponse(10L, 1L, "kamal@example", 1L, LocalDate.of(2026, 1, 1),LocalDate.of(2026,1,4), BookingStatus.PENDING, new BigDecimal("350"));

        when(bookingTransactionService.saveBookingWithLock(bookingRequest)).thenReturn(bookingResponse);

        BookingResponse result =  bookingService.createBooking(bookingRequest);

        assertEquals(bookingResponse, result);

        verify(bookingTransactionService).saveBookingWithLock(bookingRequest);

        ArgumentCaptor<PaymentRequestEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentRequestEvent.class);

        verify(paymentRequestProducer).sendPaymentRequest(eventCaptor.capture());

        PaymentRequestEvent event = eventCaptor.getValue();

        assertEquals(bookingResponse.getId(), event.bookingId());
        assertEquals(bookingResponse.getUserId(), event.userId());
        assertEquals(bookingResponse.getPrice(), event.amount());
    }

    @Test
    void shouldNotSendPaymentRequest_whenBookingCreationFails() {
        BookingRequest bookingRequest = new BookingRequest(1L, LocalDate.of(2026,1,1), LocalDate.of(2026,1,4));

        when(bookingTransactionService.saveBookingWithLock(bookingRequest)).thenThrow(new InvalidBookingDatesException("Check-out date must be after check-in date"));

        assertThrows(InvalidBookingDatesException.class, () -> bookingService.createBooking(bookingRequest));

        verify(paymentRequestProducer, never()).sendPaymentRequest(any(PaymentRequestEvent.class));
    }

    @Test
    void shouldThrowBookingNotFoundException_whenBookingNotFound() {
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingById(bookingId));

        verify(bookingRepository).findById(bookingId);
        verify(userClient, never()).getUser(any());
    }

    @Test
    void shouldThrowBookingNotFoundException_whenBookingBelongsToAnotherUser() {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUserId(2L);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingById(bookingId)
        );

        verify(bookingRepository).findById(bookingId);
        verify(userClient, never()).getUser(any());
    }

    @Test
    void shouldReturnBooking_whenBookingBelongsToCurrentUser() {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUserId(1L);
        booking.setRoomId(2L);
        booking.setCheckInDate(LocalDate.of(2026, 1, 1));
        booking.setCheckOutDate(LocalDate.of(2026, 1, 4));
        booking.setPrice(new BigDecimal("300"));
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        UserResponse userResponse = new UserResponse(
                "Kamal",
                "Mammadli",
                "kamal@gmail.com",
                "kamal-phone"
        );

        when(userClient.getUser(booking.getUserId()))
                .thenReturn(userResponse);


        BookingResponse result = bookingService.getBookingById(bookingId);

        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getUserId(), result.getUserId());
        assertEquals(booking.getRoomId(), result.getRoomId());
        assertEquals(booking.getCheckInDate(), result.getCheckInDate());
        assertEquals(booking.getCheckOutDate(), result.getCheckOutDate());
        assertEquals(booking.getPrice(), result.getPrice());
        assertEquals("kamal@gmail.com", result.getUserEmail());

        verify(bookingRepository).findById(bookingId);
        verify(userClient).getUser(booking.getUserId());
    }


    @Test
    void shouldReturnBookingWithoutUserEmail_whenUserServiceFails() {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUserId(1L);
        booking.setRoomId(2L);
        booking.setCheckInDate(LocalDate.of(2026, 1, 1));
        booking.setCheckOutDate(LocalDate.of(2026, 1, 4));
        booking.setPrice(new BigDecimal("300"));
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userClient.getUser(booking.getUserId()))
                .thenThrow(new RestClientException("User service is unavailable"));


        BookingResponse result = bookingService.getBookingById(bookingId);


        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getUserId(), result.getUserId());
        assertEquals(booking.getRoomId(), result.getRoomId());
        assertNull(result.getUserEmail());

        verify(userClient).getUser(booking.getUserId());
    }
}
