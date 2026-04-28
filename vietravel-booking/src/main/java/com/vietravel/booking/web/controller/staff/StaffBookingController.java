package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.service.booking.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/bookings")
public class StaffBookingController {

     private final BookingRepository bookingRepository;
     private final BookingService bookingService;

     public StaffBookingController(BookingRepository bookingRepository, BookingService bookingService) {
          this.bookingRepository = bookingRepository;
          this.bookingService = bookingService;
     }

     @GetMapping("/create")
     public String create(Model model) {
          model.addAttribute("pageTitle", "Tạo booking");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "create");
          return "staff/bookings/create";
     }

     @GetMapping("/process")
     public String process(Model model,
               @RequestParam(value = "from", required = false) LocalDate from,
               @RequestParam(value = "to", required = false) LocalDate to,
               @RequestParam(value = "status", required = false) BookingStatus status,
               @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
          model.addAttribute("pageTitle", "Xử lý đặt tour");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "process");
          LocalDateTime start = null;
          LocalDateTime end = null;
          if (from != null) {
               start = from.atStartOfDay();
          }
          if (to != null) {
               end = to.plusDays(1).atStartOfDay();
          }

          int pageSize = 5;
          int currentPage = Math.max(1, page);

          var allViews = (start != null && end != null)
                    ? bookingService.buildBookingViews(
                              bookingRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end), false)
                    : bookingService.buildBookingViews(bookingRepository.findTop50ByOrderByCreatedAtDesc(), false);

          if (status != null) {
               allViews = allViews.stream()
                         .filter(view -> view.getBooking() != null && status.equals(view.getBooking().getStatus()))
                         .collect(Collectors.toList());
          }

          int totalItems = allViews.size();
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
          int toIndex = Math.min(fromIndex + pageSize, totalItems);

          model.addAttribute("bookingViews", allViews.subList(fromIndex, toIndex));
          model.addAttribute("currentPage", currentPage);
          model.addAttribute("totalPages", Math.max(totalPages, 1));
          model.addAttribute("selectedFrom", from != null ? from.toString() : "");
          model.addAttribute("selectedTo", to != null ? to.toString() : "");
          model.addAttribute("selectedStatus", status != null ? status.name() : "");
          model.addAttribute("statusOptions", BookingStatus.values());
          return "staff/bookings/process";
     }

     @GetMapping("/{id}")
     public String detail(@PathVariable Long id, Model model) {
          model.addAttribute("pageTitle", "Chi tiết đặt tour");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "process");
          model.addAttribute("bookingView", bookingService.getBookingViewById(id, false));
          return "staff/bookings/detail";
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

     @PostMapping("/{id}/confirm")
     public String confirm(@PathVariable Long id) {
          boolean ok = bookingService.confirmBookingByStaff(id);
          return "redirect:/staff/bookings/process?toast=" + (ok ? "confirm-success" : "confirm-failed");
     }

     @PostMapping("/{id}/cancel")
     public String cancel(@PathVariable Long id,
               @RequestParam(value = "cancelReason", required = false) String cancelReason,
               @RequestParam(value = "cancelReasonOther", required = false) String cancelReasonOther) {
          String reason = resolveCancelReason(cancelReason, cancelReasonOther);
          boolean ok = bookingService.cancelBookingByStaff(id, reason);
          return "redirect:/staff/bookings/process?toast=" + (ok ? "cancel-success" : "cancel-failed");
     }

     private String resolveCancelReason(String cancelReason, String cancelReasonOther) {
          if (cancelReason == null || cancelReason.isBlank()) {
               return null;
          }
          switch (cancelReason) {
               case "CUSTOMER_REQUEST":
                    return "Khách yêu cầu hủy";
               case "PAYMENT_FAILED":
                    return "Thanh toán thất bại";
               case "NO_SHOW":
                    return "Khách không xác nhận";
               case "SOLD_OUT":
                    return "Hết chỗ";
               case "SCHEDULE_CHANGED":
                    return "Thay đổi lịch khởi hành";
               case "OTHER":
                    return cancelReasonOther != null && !cancelReasonOther.isBlank()
                              ? cancelReasonOther.trim()
                              : null;
               default:
                    return cancelReason;
          }
     }
}
