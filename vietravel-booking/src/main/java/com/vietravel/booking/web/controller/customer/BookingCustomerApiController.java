package com.vietravel.booking.web.controller.customer;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.service.payment.PaymentService;
import com.vietravel.booking.service.payment.VnpayService;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest;
import com.vietravel.booking.web.dto.booking.BookingCreateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/customer/bookings")
public class BookingCustomerApiController {

     private final BookingService bookingService;
     private final PaymentService paymentService;
     private final VnpayService vnpayService;

     public BookingCustomerApiController(BookingService bookingService,
               PaymentService paymentService,
               VnpayService vnpayService) {
          this.bookingService = bookingService;
          this.paymentService = paymentService;
          this.vnpayService = vnpayService;
     }

     @PostMapping
     public ResponseEntity<BookingCreateResponse> create(@RequestBody BookingCreateRequest req) {
          Booking booking = bookingService.createBooking(req);
          BookingCreateResponse res = new BookingCreateResponse(booking.getBookingCode(), booking.getStatus());
          return ResponseEntity.ok(res);
     }

     @PostMapping("/pay")
     public ResponseEntity<BookingCreateResponse> createAndPay(@RequestBody BookingCreateRequest req,
               HttpServletRequest request) {
          Booking booking = bookingService.createBooking(req);
          var payment = paymentService.createPaymentForBooking(booking);
          String ipAddress = getClientIp(request);
          String paymentUrl = vnpayService.buildPaymentUrl(payment, booking, ipAddress);
          BookingCreateResponse res = new BookingCreateResponse(booking.getBookingCode(), booking.getStatus(),
                    paymentUrl);
          return ResponseEntity.ok(res);
     }

     private String getClientIp(HttpServletRequest request) {
          if (request == null) {
               return "127.0.0.1";
          }
          String forwarded = request.getHeader("X-Forwarded-For");
          if (forwarded != null && !forwarded.isBlank()) {
               return forwarded.split(",")[0].trim();
          }
          String realIp = request.getHeader("X-Real-IP");
          if (realIp != null && !realIp.isBlank()) {
               return realIp;
          }
          return request.getRemoteAddr();
     }
}
