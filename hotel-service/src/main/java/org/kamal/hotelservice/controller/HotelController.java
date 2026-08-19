package org.kamal.hotelservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kamal.hotelservice.dto.request.HotelRequest;
import org.kamal.hotelservice.dto.response.HotelResponse;
import org.kamal.hotelservice.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelRequest hotelRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createHotel(hotelRequest));
    }
}
