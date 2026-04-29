package com.vietravel.booking.web.controller.admin.view;

import com.vietravel.booking.domain.entity.auth.UserProfile;
import com.vietravel.booking.domain.entity.booking.Payment;
import com.vietravel.booking.domain.entity.booking.PaymentMethod;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import com.vietravel.booking.domain.entity.booking.PaymentType;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentViewController {

     private final PaymentRepository paymentRepository;

     public AdminPaymentViewController(PaymentRepository paymentRepository) {
          this.paymentRepository = paymentRepository;
     }

     @GetMapping
     public String index(Model model,
               @RequestParam(value = "status", required = false) PaymentStatus status,
               @RequestParam(value = "method", required = false) PaymentMethod method,
               @RequestParam(value = "type", required = false) PaymentType type,
               @RequestParam(value = "q", required = false) String q,
               @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
          model.addAttribute("pageTitle", "Theo dõi thanh toán");
          model.addAttribute("activeMenu", "payments");
          model.addAttribute("activeSubMenu", "");

          List<Payment> payments = paymentRepository.findTop50ByOrderByCreatedAtDesc();
          String keyword = normalize(q);

          if (status != null) {
               payments = payments.stream()
                         .filter(p -> status.equals(p.getStatus()))
                         .collect(Collectors.toList());
          }

          if (method != null) {
               payments = payments.stream()
                         .filter(p -> method.equals(p.getMethod()))
                         .collect(Collectors.toList());
          }

          if (type != null) {
               payments = payments.stream()
                         .filter(p -> type.equals(p.getPaymentType()))
                         .collect(Collectors.toList());
          }

          if (!keyword.isEmpty()) {
               payments = payments.stream()
                         .filter(p -> matchesKeyword(p, keyword))
                         .collect(Collectors.toList());
          }

          int pageSize = 10;
          int totalItems = payments.size();
          int currentPage = Math.max(1, page);
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
          int toIndex = Math.min(fromIndex + pageSize, totalItems);

          model.addAttribute("payments", payments.subList(fromIndex, toIndex));
          model.addAttribute("currentPage", currentPage);
          model.addAttribute("totalPages", Math.max(totalPages, 1));
          model.addAttribute("selectedStatus", status != null ? status.name() : "");
          model.addAttribute("selectedMethod", method != null ? method.name() : "");
          model.addAttribute("selectedType", type != null ? type.name() : "");
          model.addAttribute("selectedQuery", q != null ? q : "");
          model.addAttribute("statusOptions", PaymentStatus.values());
          model.addAttribute("methodOptions", PaymentMethod.values());
          model.addAttribute("typeOptions", PaymentType.values());
          return "admin/payments/page";
     }

     @GetMapping("/{id}")
     public String detail(@PathVariable Long id, Model model) {
          model.addAttribute("pageTitle", "Chi tiết thanh toán");
          model.addAttribute("activeMenu", "payments");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("payment", paymentRepository.findWithBookingById(id).orElse(null));
          return "admin/payments/detail";
     }

     private boolean matchesKeyword(Payment payment, String keyword) {
          if (payment == null) {
               return false;
          }
          if (contains(payment.getTxnRef(), keyword)) {
               return true;
          }
          if (payment.getBooking() != null) {
               if (contains(payment.getBooking().getBookingCode(), keyword)
                         || contains(payment.getBooking().getContactName(), keyword)
                         || contains(payment.getBooking().getContactEmail(), keyword)
                         || contains(payment.getBooking().getContactPhone(), keyword)) {
                    return true;
               }
               if (payment.getBooking().getDeparture() != null
                         && payment.getBooking().getDeparture().getTour() != null
                         && contains(payment.getBooking().getDeparture().getTour().getTitle(), keyword)) {
                    return true;
               }
               if (payment.getBooking().getUser() != null) {
                    if (contains(payment.getBooking().getUser().getEmail(), keyword)) {
                         return true;
                    }
                    UserProfile profile = payment.getBooking().getUser().getProfile();
                    if (profile != null && contains(profile.getFullName(), keyword)) {
                         return true;
                    }
               }
          }
          return false;
     }

     private String normalize(String value) {
          return value == null ? "" : value.trim().toLowerCase();
     }

     private boolean contains(String value, String keyword) {
          if (value == null) {
               return false;
          }
          return value.toLowerCase().contains(keyword);
     }
}
