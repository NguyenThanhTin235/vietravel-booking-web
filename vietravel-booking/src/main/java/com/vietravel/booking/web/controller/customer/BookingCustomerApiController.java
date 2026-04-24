package com.vietravel.booking.web.controller.customer;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest;
import com.vietravel.booking.web.dto.booking.BookingCreateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/bookings")
public class BookingCustomerApiController {

     private final BookingService bookingService;

     public BookingCustomerApiController(BookingService bookingService) {
          this.bookingService = bookingService;
     }

     @PostMapping
     public ResponseEntity<BookingCreateResponse> create(@RequestBody BookingCreateRequest req) {
          Booking booking = bookingService.createBooking(req);
          BookingCreateResponse res = new BookingCreateResponse(booking.getBookingCode(), booking.getStatus());
          return ResponseEntity.ok(res);
     }
}
