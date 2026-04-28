package com.vietravel.booking.web.controller.staff.api;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.service.payment.PaymentService;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest;
import com.vietravel.booking.web.dto.booking.BookingCreateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/bookings")
public class StaffBookingApiController {

     private final BookingService bookingService;
     private final PaymentService paymentService;

     public StaffBookingApiController(BookingService bookingService, PaymentService paymentService) {
          this.bookingService = bookingService;
          this.paymentService = paymentService;
     }

     @PostMapping
     public ResponseEntity<BookingCreateResponse> create(@RequestBody BookingCreateRequest req) {
          try {
               Booking booking = bookingService.createBookingByStaff(req);
               paymentService.createCounterPaymentSuccess(booking);
               return ResponseEntity.ok(new BookingCreateResponse(booking.getBookingCode(), booking.getStatus()));
          } catch (IllegalArgumentException ex) {
               return ResponseEntity.badRequest().body(new BookingCreateResponse());
          }
     }
}
