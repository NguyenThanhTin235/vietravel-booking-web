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

import java.util.ArrayList;
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
          model.addAttribute("cancelHistoryPairs", buildCancelHistoryPairs(view.getBooking().getNote()));
          return "public/bookings/history-detail";
     }

     @PostMapping("/{id}/cancel")
     public String cancel(@PathVariable("id") Long id) {
          return "redirect:/customer/bookings/" + id;
     }

     @PostMapping("/{id}/cancel-request")
     public String requestCancel(@PathVariable("id") Long id,
               @RequestParam(value = "cancelReason", required = false) String cancelReason,
               @RequestParam(value = "cancelReasonOther", required = false) String cancelReasonOther) {
          String reason = resolveCancelReason(cancelReason, cancelReasonOther);
          boolean requested = bookingService.requestCancelMyBooking(id, reason);
          return "redirect:/customer/bookings/" + id + "?cancelRequest=" + (requested ? "success" : "failed");
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

     private List<CancelHistoryPair> buildCancelHistoryPairs(String note) {
          if (note == null || note.isBlank()) {
               return List.of();
          }
          List<CancelHistoryPair> pairs = new ArrayList<>();
          CancelHistoryPair current = null;
          String[] lines = note.split("\\n");
          for (String raw : lines) {
               String line = raw == null ? "" : raw.trim();
               if (line.isEmpty()) {
                    continue;
               }
               if (line.startsWith("Yêu cầu hủy (KH):")) {
                    current = new CancelHistoryPair();
                    current.request = line.replace("Yêu cầu hủy (KH):", "").trim();
                    pairs.add(current);
                    continue;
               }
               if (line.startsWith("Duyệt hủy (NV):")) {
                    if (current == null) {
                         current = new CancelHistoryPair();
                         pairs.add(current);
                    }
                    current.response = line.replace("Duyệt hủy (NV):", "").trim();
                    current.responseType = "approve";
                    current = null;
                    continue;
               }
               if (line.startsWith("Từ chối hủy (NV):")) {
                    if (current == null) {
                         current = new CancelHistoryPair();
                         pairs.add(current);
                    }
                    current.response = line.replace("Từ chối hủy (NV):", "").trim();
                    current.responseType = "reject";
                    current = null;
                    continue;
               }
               if (line.startsWith("Lý do hủy (NV):")) {
                    if (current == null) {
                         current = new CancelHistoryPair();
                         pairs.add(current);
                    }
                    current.response = line.replace("Lý do hủy (NV):", "").trim();
                    current.responseType = "staff";
                    current = null;
               }
          }
          return pairs;
     }

     public static class CancelHistoryPair {
          private String request;
          private String response;
          private String responseType;

          public String getRequest() {
               return request;
          }

          public String getResponse() {
               return response;
          }

          public String getResponseType() {
               return responseType;
          }
     }
}
