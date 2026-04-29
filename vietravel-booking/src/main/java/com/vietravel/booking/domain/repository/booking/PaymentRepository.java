package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
     Optional<Payment> findByTxnRef(String txnRef);

     Optional<Payment> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
