package org.kamal.hotelservice.service;

import lombok.RequiredArgsConstructor;
import org.kamal.hotelservice.dto.request.HotelRequest;
import org.kamal.hotelservice.dto.response.HotelResponse;
import org.kamal.hotelservice.entity.Hotel;
import org.kamal.hotelservice.exception.HotelAlreadyExistsException;
import org.kamal.hotelservice.repository.HotelRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelResponse createHotel(HotelRequest hotelRequest) {
        if(hotelRepository.existsByCityAndAddress(hotelRequest.getCity(), hotelRequest.getAddress())) {
            throw new HotelAlreadyExistsException("Hotel already exists");
        }
        Hotel hotel = new Hotel();
        hotel.setName(hotelRequest.getName());
        hotel.setCity(hotelRequest.getCity());
        hotel.setAddress(hotelRequest.getAddress());
        hotel.setStars(hotelRequest.getStars());

        hotelRepository.save(hotel);
        return HotelResponse.fromEntity(hotel);
    }
}
