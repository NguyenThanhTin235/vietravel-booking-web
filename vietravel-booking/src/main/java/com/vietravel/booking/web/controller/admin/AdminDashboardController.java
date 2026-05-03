package com.vietravel.booking.web.controller.admin;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

     private final TourRepository tourRepository;
     private final DepartureRepository departureRepository;
     private final BookingRepository bookingRepository;
     private final PaymentRepository paymentRepository;

     public AdminDashboardController(
               TourRepository tourRepository,
               DepartureRepository departureRepository,
               BookingRepository bookingRepository,
               PaymentRepository paymentRepository) {
          this.tourRepository = tourRepository;
          this.departureRepository = departureRepository;
          this.bookingRepository = bookingRepository;
          this.paymentRepository = paymentRepository;
     }

     @GetMapping
     public String dashboard(Model model) {
          LocalDate today = LocalDate.now();
          LocalDate to = today.plusDays(30);

          long kpiTours = tourRepository.count();
          long kpiDepartures = departureRepository.countByStartDateBetween(today, to);
          long kpiPending = bookingRepository.countByStatus(BookingStatus.PENDING);
          BigDecimal revenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);

          model.addAttribute("kpiTours", kpiTours);
          model.addAttribute("kpiDepartures", kpiDepartures);
          model.addAttribute("kpiPending", kpiPending);
          model.addAttribute("kpiRevenueRaw", revenue != null ? revenue : BigDecimal.ZERO);

          model.addAttribute("recentBookings", bookingRepository.findTop5ByOrderByCreatedAtDesc());
          model.addAttribute("upcomingDepartures",
                    departureRepository.findUpcoming(today, PageRequest.of(0, 3)));

          // Booking chart data (7 days)
          List<String> bookingChartLabels = new ArrayList<>();
          List<Long> bookingChartData = new ArrayList<>();
          for (int i = 6; i >= 0; i--) {
               LocalDate d = today.minusDays(i);
               long count = bookingRepository.countByCreatedAtBetween(
                         d.atStartOfDay(), d.plusDays(1).atStartOfDay());
               bookingChartLabels.add(d.format(DateTimeFormatter.ofPattern("dd/MM")));
               bookingChartData.add(count);
          }
          model.addAttribute("bookingChartLabels", bookingChartLabels);
          model.addAttribute("bookingChartData", bookingChartData);

          CalendarView calendarView = buildCalendar(today);
          model.addAttribute("calendarLabel", calendarView.label());
          model.addAttribute("calendarDays", calendarView.days());

          model.addAttribute("pageTitle", "Bảng điều khiển");
          model.addAttribute("activeMenu", "dashboard");
          model.addAttribute("activeSubMenu", "");
          return "admin/index";
     }

     private CalendarView buildCalendar(LocalDate today) {
          LocalDate firstDay = today.withDayOfMonth(1);
          LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

          LocalDate gridStart = shiftToSunday(firstDay);
          LocalDate gridEnd = shiftToSaturday(lastDay);

          Set<LocalDate> departureDates = new HashSet<>();
          departureRepository.findForCalendar(null, firstDay, lastDay)
                    .forEach(d -> departureDates.add(d.getStartDate()));

          List<CalendarDay> days = new ArrayList<>();
          LocalDate cursor = gridStart;
          while (!cursor.isAfter(gridEnd)) {
               days.add(new CalendarDay(
                         cursor.getDayOfMonth(),
                         cursor.getMonthValue() == today.getMonthValue(),
                         cursor.equals(today),
                         departureDates.contains(cursor)));
               cursor = cursor.plusDays(1);
          }

          String label = today.format(DateTimeFormatter.ofPattern("'Tháng' MM/yyyy", new Locale("vi")));
          return new CalendarView(label, days);
     }

     private LocalDate shiftToSunday(LocalDate date) {
          DayOfWeek dow = date.getDayOfWeek();
          int shift = dow.getValue() % 7;
          return date.minusDays(shift);
     }

     private LocalDate shiftToSaturday(LocalDate date) {
          DayOfWeek dow = date.getDayOfWeek();
          int shift = 6 - (dow.getValue() % 7);
          return date.plusDays(shift);
     }

     public record CalendarDay(int day, boolean inMonth, boolean today, boolean hasDeparture) {
     }

     public record CalendarView(String label, List<CalendarDay> days) {
     }
}
