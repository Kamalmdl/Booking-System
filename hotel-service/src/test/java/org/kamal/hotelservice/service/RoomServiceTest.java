package org.kamal.hotelservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kamal.hotelservice.dto.request.RoomRequest;
import org.kamal.hotelservice.dto.response.RoomResponse;
import org.kamal.hotelservice.entity.Hotel;
import org.kamal.hotelservice.entity.Room;
import org.kamal.hotelservice.entity.RoomType;
import org.kamal.hotelservice.exception.HotelNotFoundException;
import org.kamal.hotelservice.exception.RoomNotFoundException;
import org.kamal.hotelservice.repository.HotelRepository;
import org.kamal.hotelservice.repository.RoomRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    private RoomRequest roomRequest;

    @BeforeEach
    void setUp() {
        roomRequest = new RoomRequest(1L, RoomType.DELUXE, 10, BigDecimal.valueOf(100));
    }
    @InjectMocks
    private RoomService roomService;

    @Test
    void shouldThrowHotelNotFoundException_whenHotelNotFound() {
        when(hotelRepository.findById(roomRequest.getHotelId())).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> roomService.createRoom(roomRequest));
    }

    @Test
    void shouldNotSaveRoom_whenHotelNotFound() {
        when(hotelRepository.findById(roomRequest.getHotelId())).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> roomService.createRoom(roomRequest));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void shouldSaveCorrectRoomFields_whenHotelFound() {
        Hotel hotel = new Hotel();
        hotel.setId(roomRequest.getHotelId());
        when(hotelRepository.findById(roomRequest.getHotelId())).thenReturn(Optional.of(hotel));

        roomService.createRoom(roomRequest);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        Room room = roomCaptor.getValue();

        assertEquals(roomRequest.getCapacity(), room.getCapacity());
        assertEquals(roomRequest.getPricePerNight(), room.getPricePerNight());
        assertEquals(roomRequest.getType(), room.getType());
        assertEquals(hotel, room.getHotel());
    }

    @Test
    void shouldMapRoomToRoomResponse_whenRoomFound() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Hotel 1");

        Room room = new Room();
        room.setId(1L);
        room.setHotel(hotel);
        room.setType(RoomType.DELUXE);
        room.setCapacity(10);
        room.setPricePerNight(new BigDecimal("10"));

        when(roomRepository.findByHotelId(1L))
                .thenReturn(List.of(room));

        List<RoomResponse> result = roomService.getRoomsByHotelId(1L);

        assertEquals(1, result.size());

        RoomResponse response = result.get(0);

        assertEquals(room.getId(), response.getId());
        assertEquals(room.getType(), response.getType());
        assertEquals(room.getCapacity(), response.getCapacity());
        assertEquals(room.getPricePerNight(), response.getPricePerNight());
        assertEquals(hotel.getId(), response.getHotelId());
        assertEquals(hotel.getName(), response.getHotelName());
    }

    @Test
    void shouldReturnEmptyList_whenRoomsNotFoundByHotelId() {
        when(roomRepository.findByHotelId(roomRequest.getHotelId())).thenReturn(List.of());

        List<RoomResponse> result = roomService.getRoomsByHotelId(roomRequest.getHotelId());

        assertTrue(result.isEmpty());
        verify(roomRepository).findByHotelId(roomRequest.getHotelId());
    }

    @Test
    void shouldThrowRoomNotFoundException_whenRoomNotFound() {
        Long roomId = 1L;

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(roomId));
    }

    @Test
    void shouldReturnRoom_whenRoomFound() {
        Long roomId = 1L;
        Long hotelId = 1L;

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Hotel 1");

        Room room = new Room();
        room.setId(roomId);
        room.setHotel(hotel);
        room.setType(RoomType.DELUXE);
        room.setCapacity(10);
        room.setPricePerNight(new BigDecimal("10"));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        RoomResponse response = roomService.getRoomById(roomId);

        assertEquals(room.getId(), response.getId());
        assertEquals(room.getType(), response.getType());
        assertEquals(room.getCapacity(), response.getCapacity());
        assertEquals(room.getPricePerNight(), response.getPricePerNight());
        assertEquals(hotel.getId(), response.getHotelId());
        assertEquals(hotel.getName(), response.getHotelName());
    }
}
