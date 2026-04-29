package com.vietravel.booking.web.controller.admin.view;

import com.vietravel.booking.domain.entity.auth.UserProfile;
import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.web.dto.booking.BookingHistoryView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingViewController {

     private final BookingRepository bookingRepository;
     private final BookingService bookingService;

     public AdminBookingViewController(BookingRepository bookingRepository, BookingService bookingService) {
          this.bookingRepository = bookingRepository;
          this.bookingService = bookingService;
     }

     @GetMapping
     public String index(Model model,
               @RequestParam(value = "status", required = false) BookingStatus status,
               @RequestParam(value = "customer", required = false) String customer,
               @RequestParam(value = "tour", required = false) String tour,
               @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
          model.addAttribute("pageTitle", "Booking");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "");

          List<Booking> bookings = status != null
                    ? bookingRepository.findTop50ByStatusOrderByCreatedAtDesc(status)
                    : bookingRepository.findTop50ByOrderByCreatedAtDesc();

          List<BookingHistoryView> allViews = bookingService.buildBookingViews(bookings, false);

          String customerKey = normalize(customer);
          String tourKey = normalize(tour);

          if (!customerKey.isEmpty()) {
               allViews = allViews.stream()
                         .filter(view -> matchesCustomer(view, customerKey))
                         .collect(Collectors.toList());
          }

          if (!tourKey.isEmpty()) {
               allViews = allViews.stream()
                         .filter(view -> matchesTour(view, tourKey))
                         .collect(Collectors.toList());
          }

          int pageSize = 10;
          int totalItems = allViews.size();
          int currentPage = Math.max(1, page);
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
          int toIndex = Math.min(fromIndex + pageSize, totalItems);

          model.addAttribute("bookingViews", allViews.subList(fromIndex, toIndex));
          model.addAttribute("currentPage", currentPage);
          model.addAttribute("totalPages", Math.max(totalPages, 1));
          model.addAttribute("selectedStatus", status != null ? status.name() : "");
          model.addAttribute("selectedCustomer", customer != null ? customer : "");
          model.addAttribute("selectedTour", tour != null ? tour : "");
          model.addAttribute("statusOptions", BookingStatus.values());
          return "admin/bookings/page";
     }

     @GetMapping("/{id}")
     public String detail(@PathVariable Long id, Model model) {
          model.addAttribute("pageTitle", "Chi tiết booking");
          model.addAttribute("activeMenu", "bookings");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("bookingView", bookingService.getBookingViewById(id, false));
          return "admin/bookings/detail";
     }

     private boolean matchesCustomer(BookingHistoryView view, String key) {
          if (view == null || view.getBooking() == null) {
               return false;
          }
          Booking booking = view.getBooking();
          if (contains(booking.getContactName(), key)
                    || contains(booking.getContactEmail(), key)
                    || contains(booking.getContactPhone(), key)
                    || contains(booking.getBookingCode(), key)) {
               return true;
          }
          if (booking.getUser() != null) {
               if (contains(booking.getUser().getEmail(), key)) {
                    return true;
               }
               UserProfile profile = booking.getUser().getProfile();
               if (profile != null && contains(profile.getFullName(), key)) {
                    return true;
               }
          }
          return false;
     }

     private boolean matchesTour(BookingHistoryView view, String key) {
          if (view == null || view.getBooking() == null) {
               return false;
          }
          Booking booking = view.getBooking();
          if (booking.getDeparture() == null || booking.getDeparture().getTour() == null) {
               return false;
          }
          return contains(booking.getDeparture().getTour().getTitle(), key)
                    || contains(booking.getDeparture().getTour().getCode(), key);
     }

     private String normalize(String value) {
          return value == null ? "" : value.trim().toLowerCase();
     }

     private boolean contains(String value, String key) {
          if (value == null) {
               return false;
          }
          return value.toLowerCase().contains(key);
     }
}
