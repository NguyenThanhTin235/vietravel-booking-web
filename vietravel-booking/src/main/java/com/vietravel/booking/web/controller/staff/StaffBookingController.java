package com.vietravel.booking.web.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/bookings")
public class StaffBookingController {

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
          return "staff/bookings/process";
     }

     @GetMapping("/cancel-requests")
     public String cancelRequests(Model model) {
          model.addAttribute("pageTitle", "Yêu cầu hủy tour");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "cancel-requests");
          return "staff/bookings/cancel-requests";
     }
}
