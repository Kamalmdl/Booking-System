package org.kamal.bookingservice.repository;

import jakarta.persistence.LockModeType;
import org.kamal.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId AND b.status != 'CANCELLED' AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId, @Param("checkInDate") LocalDate checkInDate,  @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findAllByUserId(Long userId);
}
