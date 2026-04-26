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

@Controller
@RequestMapping("/staff/stats")
public class StaffStatsController {

     private final BookingRepository bookingRepository;

     public StaffStatsController(BookingRepository bookingRepository) {
          this.bookingRepository = bookingRepository;
     }

     @GetMapping
     public String stats(Model model) {
          LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
          LocalDateTime endOfDay = startOfDay.plusDays(1);
          LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
          LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

          long totalMonth = bookingRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
          long canceledMonth = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.CANCELED, startOfMonth, endOfMonth);
          double cancelRate = totalMonth > 0 ? (canceledMonth * 100.0 / totalMonth) : 0.0;

          model.addAttribute("kpiMonth", totalMonth);
          model.addAttribute("kpiCancelRate", cancelRate);
          model.addAttribute("kpiProcessedToday", bookingRepository.countByUpdatedAtBetween(startOfDay, endOfDay));
          model.addAttribute("kpiRevenueMonth", bookingRepository.sumTotalAmountBetween(startOfMonth, endOfMonth));
          model.addAttribute("dailyCounts", bookingRepository.findDailyCountsSince(startOfMonth));
          model.addAttribute("topTours", bookingRepository.findTopTours(PageRequest.of(0, 3)));

          model.addAttribute("pageTitle", "Thống kê");
          model.addAttribute("activeMenu", "stats");
          model.addAttribute("activeSubMenu", "");
          return "staff/stats/index";
     }
}
