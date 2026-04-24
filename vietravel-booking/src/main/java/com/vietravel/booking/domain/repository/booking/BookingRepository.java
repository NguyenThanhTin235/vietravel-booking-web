package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
