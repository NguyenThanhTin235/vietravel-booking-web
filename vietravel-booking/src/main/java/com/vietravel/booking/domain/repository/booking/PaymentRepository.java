package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Payment;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

     @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
     BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}
