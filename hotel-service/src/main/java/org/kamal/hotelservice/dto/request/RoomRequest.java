package org.kamal.hotelservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kamal.hotelservice.entity.RoomType;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {
    @NotNull(message = "Hotel ID is required!")
    private Long hotelId;
    @NotNull(message = "Type is required!")
    private RoomType type;
    @Min(value = 1, message = "Room must have at least 1 member!")
    private int capacity;
    @NotNull(message = "Price per night is required!")
    @Positive
    private BigDecimal pricePerNight;
}
