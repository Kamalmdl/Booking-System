package org.kamal.hotelservice.service;

import lombok.RequiredArgsConstructor;
import org.kamal.hotelservice.dto.request.RoomRequest;
import org.kamal.hotelservice.dto.response.RoomResponse;
import org.kamal.hotelservice.entity.Hotel;
import org.kamal.hotelservice.entity.Room;
import org.kamal.hotelservice.exception.HotelNotFoundException;
import org.kamal.hotelservice.exception.RoomNotFoundException;
import org.kamal.hotelservice.repository.HotelRepository;
import org.kamal.hotelservice.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomResponse createRoom(RoomRequest roomRequest) {
        Hotel hotel = hotelRepository.findById(roomRequest.getHotelId()).orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
        Room room = new Room();
        room.setHotel(hotel);
        room.setType(roomRequest.getType());
        room.setCapacity(roomRequest.getCapacity());
        room.setPricePerNight(roomRequest.getPricePerNight());

        roomRepository.save(room);

        return RoomResponse.fromEntity(room);
    }

    public List<RoomResponse> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream().map(RoomResponse::fromEntity).toList();
    }

    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException("Room not found."));
        return RoomResponse.fromEntity(room);
    }

}
