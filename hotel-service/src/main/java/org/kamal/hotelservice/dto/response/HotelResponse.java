package org.kamal.hotelservice.dto.response;

import lombok.*;
import org.kamal.hotelservice.entity.Hotel;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private int stars;

    public static HotelResponse fromEntity(Hotel hotel) {
        return new HotelResponse(hotel.getId(), hotel.getName(), hotel.getAddress(), hotel.getCity(), hotel.getStars());
    }
}
