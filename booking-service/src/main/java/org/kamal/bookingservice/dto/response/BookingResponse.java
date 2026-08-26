package org.kamal.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kamal.bookingservice.entity.Booking;
import org.kamal.bookingservice.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;

    private Long userId;

    private String userEmail;

    private Long roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BookingStatus status;

    private BigDecimal price;

    public static BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getUserId(), null, booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getStatus(), booking.getPrice());
    }
}
