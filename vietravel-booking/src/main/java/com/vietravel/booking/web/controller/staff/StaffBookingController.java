package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/bookings")
public class StaffBookingController {

     private final BookingRepository bookingRepository;

     public StaffBookingController(BookingRepository bookingRepository) {
          this.bookingRepository = bookingRepository;
     }

     @GetMapping("/create")
     public String create(Model model) {
          model.addAttribute("pageTitle", "Tạo booking");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "create");
          return "staff/bookings/create";
     }

     @GetMapping("/process")
     public String process(Model model) {
          model.addAttribute("pageTitle", "Xử lý đặt tour");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "process");
          model.addAttribute("bookings", bookingRepository.findTop50ByOrderByCreatedAtDesc());
          return "staff/bookings/process";
     }

     @GetMapping("/cancel-requests")
     public String cancelRequests(Model model) {
          model.addAttribute("pageTitle", "Yêu cầu hủy tour");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "cancel-requests");
          model.addAttribute("cancelBookings", bookingRepository.findTop50ByStatusOrderByCreatedAtDesc(
                  BookingStatus.CANCEL_REQUESTED));
          return "staff/bookings/cancel-requests";
     }
}
