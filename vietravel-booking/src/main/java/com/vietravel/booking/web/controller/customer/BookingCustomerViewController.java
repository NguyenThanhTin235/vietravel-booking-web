package com.vietravel.booking.web.controller.customer;

import com.vietravel.booking.service.booking.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/bookings")
public class BookingCustomerViewController {

     private final BookingService bookingService;

     public BookingCustomerViewController(BookingService bookingService) {
          this.bookingService = bookingService;
     }

     @GetMapping
     public String list(Model model) {
          model.addAttribute("pageTitle", "Tour đã đặt");
          model.addAttribute("activeNav", "profile");
          model.addAttribute("bookings", bookingService.getMyBookings());
          return "public/bookings/history";
     }
}
