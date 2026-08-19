package org.kamal.hotelservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kamal.hotelservice.dto.request.RoomRequest;
import org.kamal.hotelservice.dto.response.RoomResponse;
import org.kamal.hotelservice.repository.RoomRepository;
import org.kamal.hotelservice.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(roomRequest));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms(@RequestParam Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

}
