package com.vietravel.booking.web.controller.publicsite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyBookingController {

     @GetMapping("/my-bookings")
     public String myBookings() {
          return "redirect:/customer/bookings";
     }
}
