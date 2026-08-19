package org.kamal.bookingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull(message = "Room is required!")
    private Long roomId;
    @NotNull(message = "Check in date is required!")
    private LocalDate checkInDate;
    @NotNull(message = "Check out date is required!")
    private LocalDate checkOutDate;
}
