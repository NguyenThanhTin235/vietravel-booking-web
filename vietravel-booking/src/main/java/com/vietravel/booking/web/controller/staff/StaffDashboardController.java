package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

     private final BookingRepository bookingRepository;
     private final PaymentRepository paymentRepository;

     public StaffDashboardController(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
          this.bookingRepository = bookingRepository;
          this.paymentRepository = paymentRepository;
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

          // Monthly revenue chart data
          List<String> revenueChartLabels = Arrays.asList(
                    "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
          BigDecimal[] revenueDataArr = new BigDecimal[12];
          for (int i = 0; i < 12; i++)
               revenueDataArr[i] = BigDecimal.ZERO;

          List<Object[]> monthlyRevenue = paymentRepository.getMonthlyRevenue(LocalDate.now().getYear());
          for (Object[] row : monthlyRevenue) {
               int month = (int) row[0];
               BigDecimal sum = (BigDecimal) row[1];
               if (month >= 1 && month <= 12) {
                    revenueDataArr[month - 1] = sum;
               }
          }
          model.addAttribute("revenueChartLabels", revenueChartLabels);
          model.addAttribute("revenueChartData", Arrays.asList(revenueDataArr));

          model.addAttribute("pageTitle", "Bảng điều khiển");
          model.addAttribute("activeMenu", "dashboard");
          model.addAttribute("activeSubMenu", "");
          return "staff/index";
     }
}
