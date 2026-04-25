package com.vietravel.booking.service.payment;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.entity.booking.Payment;
import com.vietravel.booking.domain.entity.booking.PaymentMethod;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import com.vietravel.booking.domain.entity.booking.PaymentType;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

     private final PaymentRepository paymentRepository;
     private final BookingRepository bookingRepository;

     public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
          this.paymentRepository = paymentRepository;
          this.bookingRepository = bookingRepository;
     }

     @Transactional
     public Payment createPaymentForBooking(Booking booking) {
          Payment payment = new Payment();
          payment.setBooking(booking);
          payment.setPaymentType(PaymentType.PAY);
          payment.setMethod(PaymentMethod.VNPAY_MOCK);
          payment.setStatus(PaymentStatus.INIT);
          payment.setAmount(booking.getTotalAmount() == null ? BigDecimal.ZERO : booking.getTotalAmount());
          payment.setTxnRef(generateTxnRef(booking));
          return paymentRepository.save(payment);
     }

     @Transactional
     public Booking handleVnpayReturn(String txnRef, boolean success) {
          if (txnRef == null || txnRef.isBlank()) {
               return null;
          }
          Optional<Payment> opt = paymentRepository.findByTxnRef(txnRef);
          if (opt.isEmpty()) {
               return null;
          }
          Payment payment = opt.get();
          payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
          if (success && payment.getBooking() != null) {
               payment.getBooking().setStatus(BookingStatus.PAID);
          }
          paymentRepository.save(payment);

          if (payment.getBooking() == null || payment.getBooking().getId() == null) {
               return null;
          }
          return bookingRepository.findWithDepartureTourById(payment.getBooking().getId()).orElse(null);
     }

     private String generateTxnRef(Booking booking) {
          String code = booking.getBookingCode() == null ? "BK" : booking.getBookingCode();
          String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
          String raw = code + "-" + rand;
          return raw.length() > 60 ? raw.substring(0, 60) : raw;
     }
}
