package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.repository.booking.BookingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/inquiries")
public class StaffContactController {

     private final BookingRepository bookingRepository;

     public StaffContactController(BookingRepository bookingRepository) {
          this.bookingRepository = bookingRepository;
     }

     @GetMapping
     public String inquiries(Model model) {
          var inquiries = bookingRepository.findRecentNotes(PageRequest.of(0, 20));
          model.addAttribute("inquiries", inquiries);
          model.addAttribute("selectedInquiry", inquiries.isEmpty() ? null : inquiries.get(0));
          model.addAttribute("pageTitle", "Xử lý thắc mắc");
          model.addAttribute("activeMenu", "inquiries");
          model.addAttribute("activeSubMenu", "");
          return "staff/inquiries/index";
     }
}
