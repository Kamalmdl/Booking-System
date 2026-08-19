package org.kamal.hotelservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kamal.hotelservice.entity.Room;
import org.kamal.hotelservice.entity.RoomType;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;

    private Long hotelId;

    private String hotelName;

    private RoomType type;

    private int capacity;

    private BigDecimal pricePerNight;

    public static RoomResponse fromEntity(Room room) {
        return new RoomResponse(room.getId(), room.getHotel().getId(), room.getHotel().getName(), room.getType(), room.getCapacity(), room.getPricePerNight());
    }
}
