package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

     private final BookingRepository bookingRepository;

     public StaffDashboardController(BookingRepository bookingRepository) {
          this.bookingRepository = bookingRepository;
     }

     @GetMapping
     public String dashboard(Model model) {
          LocalDateTime now = LocalDateTime.now();
          LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
          LocalDateTime endOfDay = startOfDay.plusDays(1);

          model.addAttribute("kpiToday", bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay));
          model.addAttribute("kpiPending", bookingRepository.countByStatus(BookingStatus.PENDING));
          model.addAttribute("kpiCancel", bookingRepository.countByStatus(BookingStatus.CANCEL_REQUESTED));
          model.addAttribute("kpiQuestions", bookingRepository.countNotesSince(now.minusHours(24)));

          model.addAttribute("recentBookings", bookingRepository.findTop5ByOrderByCreatedAtDesc());
          model.addAttribute("pendingBookings", bookingRepository.findTop5ByStatusInOrderByCreatedAtDesc(
                    Arrays.asList(BookingStatus.PENDING, BookingStatus.CANCEL_REQUESTED)));
          model.addAttribute("recentNotes", bookingRepository.findRecentNotes(PageRequest.of(0, 5)));
          model.addAttribute("topTours", bookingRepository.findTopTours(PageRequest.of(0, 3)));

          model.addAttribute("pageTitle", "Bảng điều khiển");
          model.addAttribute("activeMenu", "dashboard");
          model.addAttribute("activeSubMenu", "");
          return "staff/index";
     }
}
