package com.vietravel.booking.web.controller.customer;

import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import com.vietravel.booking.web.dto.booking.BookingHistoryView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/customer/bookings")
public class BookingCustomerViewController {

     private final BookingService bookingService;

     public BookingCustomerViewController(BookingService bookingService) {
          this.bookingService = bookingService;
     }

     @GetMapping
     public String list(
               @RequestParam(value = "status", required = false) String status,
               @RequestParam(value = "payment", required = false) String payment,
               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
               Model model) {
          BookingStatus bookingStatus = parseBookingStatus(status);
          PaymentStatus paymentStatus = parsePaymentStatus(payment);
          int pageSize = 5;
          model.addAttribute("pageTitle", "Tour đã đặt");
          model.addAttribute("activeNav", "profile");
          List<BookingHistoryView> all = bookingService.getMyBookings(bookingStatus, paymentStatus);
          int totalItems = all.size();
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int safePage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
          int start = (safePage - 1) * pageSize;
          int end = Math.min(start + pageSize, totalItems);
          List<BookingHistoryView> pageItems = start < end ? all.subList(start, end) : List.of();

          model.addAttribute("bookings", pageItems);
          model.addAttribute("currentPage", safePage);
          model.addAttribute("totalPages", totalPages);
          model.addAttribute("totalItems", totalItems);
          model.addAttribute("pageSize", pageSize);
          model.addAttribute("selectedStatus", bookingStatus != null ? bookingStatus.name() : "");
          model.addAttribute("selectedPayment", paymentStatus != null ? paymentStatus.name() : "");
          return "public/bookings/history";
     }

     @GetMapping("/{id}")
     public String detail(@PathVariable("id") Long id, Model model) {
          BookingHistoryView view = bookingService.getMyBookingDetail(id);
          if (view == null) {
               return "redirect:/customer/bookings";
          }
          model.addAttribute("pageTitle", "Chi tiết booking");
          model.addAttribute("activeNav", "profile");
          model.addAttribute("bookingView", view);
          return "public/bookings/history-detail";
     }

     @PostMapping("/{id}/cancel")
     public String cancel(@PathVariable("id") Long id) {
          boolean canceled = bookingService.cancelMyBooking(id);
          return "redirect:/customer/bookings/" + id + "?canceled=" + (canceled ? "success" : "failed");
     }

     private BookingStatus parseBookingStatus(String raw) {
          if (raw == null || raw.isBlank()) {
               return null;
          }
          try {
               return BookingStatus.valueOf(raw);
          } catch (IllegalArgumentException ex) {
               return null;
          }
     }

     private PaymentStatus parsePaymentStatus(String raw) {
          if (raw == null || raw.isBlank()) {
               return null;
          }
          try {
               return PaymentStatus.valueOf(raw);
          } catch (IllegalArgumentException ex) {
               return null;
          }
     }
}
