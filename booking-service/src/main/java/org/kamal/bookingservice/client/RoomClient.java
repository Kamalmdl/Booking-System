package org.kamal.bookingservice.client;

import org.kamal.bookingservice.dto.response.RoomResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RoomClient {
    private final RestClient restClient;

    public RoomClient(RestClient.Builder restClientBuilder, @Value("${hotel.service.url}") String hotelServiceHost){
        this.restClient = restClientBuilder
                .baseUrl(hotelServiceHost)
                .build();
    }

    public RoomResponse getRoomById(Long roomId) {
        return restClient.get()
                .uri("/rooms/{id}", roomId)
                .retrieve()
                .body(RoomResponse.class);
    }
}
