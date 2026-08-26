package org.kamal.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kamal.bookingservice.client.RoomClient;
import org.kamal.bookingservice.client.UserClient;
import org.kamal.bookingservice.dto.request.BookingRequest;
import org.kamal.bookingservice.dto.response.BookingResponse;
import org.kamal.bookingservice.dto.response.RoomResponse;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.entity.BookingStatus;
import org.kamal.bookingservice.event.PaymentEventProducer;
import org.kamal.bookingservice.event.PaymentRequestEvent;
import org.kamal.bookingservice.exception.*;
import org.kamal.bookingservice.repository.BookingRepository;
import org.kamal.bookingservice.security.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentEventProducer paymentRequestProducer;
    private final BookingTransactionService bookingTransactionService;
    private final UserClient userClient;


    public BookingResponse createBooking(BookingRequest bookingRequest) {
        BookingResponse booking = bookingTransactionService.saveBookingWithLock(bookingRequest);
        var event = new PaymentRequestEvent(booking.getId(), booking.getUserId(), booking.getPrice());
        paymentRequestProducer.sendPaymentRequest(event);
        return booking;
    }

    public BookingResponse getBookingById(Long id){
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = authenticatedUser.userId();
        if(!booking.getUserId().equals(userId)){
            throw new BookingNotFoundException("Booking not found");
        }
        BookingResponse response = BookingResponse.fromEntity(booking);
        try {
            response.setUserEmail(userClient.getUser(booking.getUserId()).getEmail());
        } catch(RestClientException ex) {
            log.warn("Failed to fetch {} from User service: {}", booking.getUserId(), ex.getMessage());
        }
        return response;
    }
}
