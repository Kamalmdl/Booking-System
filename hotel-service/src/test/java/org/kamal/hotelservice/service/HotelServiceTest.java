package org.kamal.hotelservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kamal.hotelservice.dto.request.HotelRequest;
import org.kamal.hotelservice.entity.Hotel;
import org.kamal.hotelservice.exception.HotelAlreadyExistsException;
import org.kamal.hotelservice.repository.HotelRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {
    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private HotelRequest hotelRequest;

    @BeforeEach
    void setUp() {
        hotelRequest = new HotelRequest("Hotel", "Address", "City", 5);
    }

    @Test
    void shouldThrowHotelAlreadyExistsException_whenHotelAlreadyExists() {
        when(hotelRepository.existsByCityAndAddress(
                hotelRequest.getCity(),
                hotelRequest.getAddress()
        )).thenReturn(true);

        assertThrows(
                HotelAlreadyExistsException.class,
                () -> hotelService.createHotel(hotelRequest)
        );
    }

    @Test
    void shouldNotSaveHotel_whenHotelAlreadyExists() {
        when(hotelRepository.existsByCityAndAddress(
                hotelRequest.getCity(),
                hotelRequest.getAddress()
        )).thenReturn(true);

        assertThrows(
                HotelAlreadyExistsException.class,
                () -> hotelService.createHotel(hotelRequest)
        );

        verify(hotelRepository, never()).save(any());
    }

    @Test
    void shouldSaveCorrectHotelFields_whenHotelDoesNotExist() {
        when(hotelRepository.existsByCityAndAddress(
                hotelRequest.getCity(),
                hotelRequest.getAddress()
        )).thenReturn(false);

        hotelService.createHotel(hotelRequest);

        ArgumentCaptor<Hotel> hotelArgumentCaptor =
                ArgumentCaptor.forClass(Hotel.class);

        verify(hotelRepository).save(hotelArgumentCaptor.capture());

        Hotel hotel = hotelArgumentCaptor.getValue();

        assertEquals(hotelRequest.getCity(), hotel.getCity());
        assertEquals(hotelRequest.getAddress(), hotel.getAddress());
        assertEquals(hotelRequest.getName(), hotel.getName());
        assertEquals(hotelRequest.getStars(), hotel.getStars());
    }
}