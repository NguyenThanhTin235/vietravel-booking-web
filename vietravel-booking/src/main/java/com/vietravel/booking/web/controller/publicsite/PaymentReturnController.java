package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.payment.PaymentService;
import com.vietravel.booking.service.payment.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/payment")
public class PaymentReturnController {

     private final VnpayService vnpayService;
     private final PaymentService paymentService;

     public PaymentReturnController(VnpayService vnpayService, PaymentService paymentService) {
          this.vnpayService = vnpayService;
          this.paymentService = paymentService;
     }

     @GetMapping("/vnpay/return")
     public Object vnpayReturn(HttpServletRequest request, Model model) {
          Map<String, String> params = new HashMap<>();
          request.getParameterMap().forEach((k, v) -> {
               if (v != null && v.length > 0) {
                    params.put(k, v[0]);
               }
          });

          String secureHash = params.get("vnp_SecureHash");
          boolean validSignature = vnpayService.verifySignature(params, secureHash);
          String responseCode = params.getOrDefault("vnp_ResponseCode", "");
          boolean success = validSignature && "00".equals(responseCode);

          var booking = validSignature
                    ? paymentService.handleVnpayReturn(params.get("vnp_TxnRef"), success)
                    : null;

          if (booking != null && booking.getDeparture() != null && booking.getDeparture().getTour() != null) {
               String slug = booking.getDeparture().getTour().getSlug();
               String date = booking.getDeparture().getStartDate() != null
                         ? booking.getDeparture().getStartDate().format(DateTimeFormatter.ISO_DATE)
                         : "";
               String code = booking.getBookingCode() != null ? booking.getBookingCode() : "";
               String target = "/tour/" + slug + "/dat?date=" + date
                         + "&payment=" + (success ? "success" : "failed")
                         + "&code=" + code;
               return new RedirectView(target, true);
          }

          model.addAttribute("pageTitle", "Kết quả thanh toán");
          model.addAttribute("activeNav", "home");
          model.addAttribute("success", success);
          model.addAttribute("message", success ? "Thanh toán thành công" : "Thanh toán không thành công");
          return "public/payments/vnpay-return";
     }
}
