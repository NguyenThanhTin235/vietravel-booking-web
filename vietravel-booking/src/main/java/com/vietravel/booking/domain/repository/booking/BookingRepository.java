package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
     Optional<Booking> findByBookingCode(String bookingCode);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     Optional<Booking> findWithDepartureTourById(Long id);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
