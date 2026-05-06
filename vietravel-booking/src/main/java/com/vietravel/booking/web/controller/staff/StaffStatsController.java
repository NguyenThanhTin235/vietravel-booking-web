package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/staff/stats")
public class StaffStatsController {

     private final BookingRepository bookingRepository;
     private final PaymentRepository paymentRepository;

     public StaffStatsController(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
          this.bookingRepository = bookingRepository;
          this.paymentRepository = paymentRepository;
     }

     @GetMapping
     public String stats(
               @RequestParam(value = "startDate", required = false) String startDateStr,
               @RequestParam(value = "endDate", required = false) String endDateStr,
               Model model) {
          LocalDateTime now = LocalDateTime.now();
          LocalDateTime startDate, endDate;
          if (startDateStr != null && !startDateStr.isEmpty()) {
               startDate = LocalDate.parse(startDateStr).atStartOfDay();
          } else {
               startDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
          }
          if (endDateStr != null && !endDateStr.isEmpty()) {
               endDate = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay();
          } else {
               endDate = startDate.plusMonths(1);
          }

          long total = bookingRepository.countByCreatedAtBetween(startDate, endDate);
          long canceled = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.CANCELED, startDate,
                    endDate);
          double cancelRate = total > 0 ? (canceled * 100.0 / total) : 0.0;

          // KPI processed today (giữ nguyên logic ngày hiện tại)
          LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
          LocalDateTime endOfDay = startOfDay.plusDays(1);

          model.addAttribute("kpiMonth", total);
          model.addAttribute("kpiCancelRate", cancelRate);
          model.addAttribute("kpiProcessedToday", bookingRepository.countByUpdatedAtBetween(startOfDay, endOfDay));
          model.addAttribute("kpiRevenueMonth", bookingRepository.sumTotalAmountBetween(startDate, endDate));
          model.addAttribute("topTours", bookingRepository.findTopTours(PageRequest.of(0, 3)));

          // Daily booking chart data
          List<String> dailyChartLabels = new ArrayList<>();
          List<Long> dailyChartData = new ArrayList<>();
          for (BookingRepository.DailyBookingCount d : bookingRepository.findDailyCountsSince(startDate)) {
               dailyChartLabels.add(d.getDay().format(DateTimeFormatter.ofPattern("dd/MM")));
               dailyChartData.add(d.getTotal());
          }
          model.addAttribute("dailyChartLabels", dailyChartLabels);
          model.addAttribute("dailyChartData", dailyChartData);

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

          model.addAttribute("startDate", startDate.toLocalDate().toString());
          model.addAttribute("endDate", endDate.minusDays(1).toLocalDate().toString());

          model.addAttribute("pageTitle", "Thống kê");
          model.addAttribute("activeMenu", "stats");
          model.addAttribute("activeSubMenu", "");
          return "staff/stats/index";
     }
}
