package org.kamal.bookingservice.service;

import lombok.RequiredArgsConstructor;
import org.kamal.bookingservice.client.RoomClient;
import org.kamal.bookingservice.dto.request.BookingRequest;
import org.kamal.bookingservice.dto.response.BookingResponse;
import org.kamal.bookingservice.dto.response.RoomResponse;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.entity.BookingStatus;
import org.kamal.bookingservice.exception.InvalidBookingDatesException;
import org.kamal.bookingservice.exception.RoomNotAvailableException;
import org.kamal.bookingservice.exception.RoomNotFoundException;
import org.kamal.bookingservice.repository.BookingRepository;
import org.kamal.bookingservice.security.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private final RoomClient roomClient;
    private final BookingRepository bookingRepository;

    @Transactional
    public BookingResponse saveBookingWithLock(BookingRequest bookingRequest) {
        if(!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingDatesException("Check-out date must be after check-in date");
        }
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = authenticatedUser.userId();
        RoomResponse room;
        try {
            room = roomClient.getRoomById(bookingRequest.getRoomId());
        } catch (HttpClientErrorException.NotFound e) {
            throw new RoomNotFoundException( "Room not found: " + bookingRequest.getRoomId());
        }
        if(!bookingRepository
                .findOverlappingBookings(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())
                .isEmpty()){
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }
        long nights = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        Booking booking = new Booking();
        booking.setPrice(totalPrice);
        booking.setRoomId(bookingRequest.getRoomId());
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        return BookingResponse.fromEntity(booking);
    }

}
