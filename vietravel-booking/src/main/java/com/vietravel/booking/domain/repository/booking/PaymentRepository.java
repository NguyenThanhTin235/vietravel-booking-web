package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
     Optional<Payment> findByTxnRef(String txnRef);

     Optional<Payment> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId);

     @EntityGraph(attributePaths = { "booking", "booking.departure", "booking.departure.tour", "booking.user",
               "booking.user.profile" })
     List<Payment> findTop50ByOrderByCreatedAtDesc();

     @EntityGraph(attributePaths = { "booking", "booking.departure", "booking.departure.tour", "booking.user",
               "booking.user.profile" })
     Optional<Payment> findWithBookingById(Long id);
}
