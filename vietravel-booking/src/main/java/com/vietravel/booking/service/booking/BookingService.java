package com.vietravel.booking.service.booking;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.BookingPassenger;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.entity.booking.PassengerType;
import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest.PassengerRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

     private final BookingRepository bookingRepository;
     private final TourRepository tourRepository;
     private final DepartureRepository departureRepository;
     private final UserAccountRepository userAccountRepository;

     public BookingService(BookingRepository bookingRepository,
               TourRepository tourRepository,
               DepartureRepository departureRepository,
               UserAccountRepository userAccountRepository) {
          this.bookingRepository = bookingRepository;
          this.tourRepository = tourRepository;
          this.departureRepository = departureRepository;
          this.userAccountRepository = userAccountRepository;
     }

     @Transactional
     public Booking createBooking(BookingCreateRequest req) {
          if (req == null) {
               throw new IllegalArgumentException("Dữ liệu không hợp lệ");
          }
          if (req.getSlug() == null || req.getSlug().isBlank() || req.getDate() == null) {
               throw new IllegalArgumentException("Thiếu thông tin tour hoặc ngày khởi hành");
          }
          if (req.getContactName() == null || req.getContactName().isBlank()
                    || req.getContactPhone() == null || req.getContactPhone().isBlank()
                    || req.getContactEmail() == null || req.getContactEmail().isBlank()) {
               throw new IllegalArgumentException("Thiếu thông tin liên lạc");
          }

          Tour tour = tourRepository.findDetailBySlug(req.getSlug())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tour"));

          Departure departure = departureRepository.findFirstByTourIdAndStartDate(tour.getId(), req.getDate())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khởi hành"));

          int totalAdult = Math.max(0, req.getTotalAdult());
          int totalChild = Math.max(0, req.getTotalChild());
          BigDecimal adultPrice = departure.getPriceAdult() != null ? departure.getPriceAdult() : BigDecimal.ZERO;
          BigDecimal childPrice = departure.getPriceChild() != null ? departure.getPriceChild() : BigDecimal.ZERO;
          BigDecimal totalAmount = adultPrice.multiply(BigDecimal.valueOf(totalAdult))
                    .add(childPrice.multiply(BigDecimal.valueOf(totalChild)));

          Booking booking = new Booking();
          booking.setBookingCode(generateBookingCode(req.getDate()));
          booking.setUser(getCurrentUser());
          booking.setDeparture(departure);
          booking.setContactName(req.getContactName().trim());
          booking.setContactPhone(req.getContactPhone().trim());
          booking.setContactEmail(req.getContactEmail().trim());
          booking.setNote(req.getNote() == null ? null : req.getNote().trim());
          booking.setTotalAdult(totalAdult);
          booking.setTotalChild(totalChild);
          booking.setTotalAmount(totalAmount);
          booking.setStatus(BookingStatus.PENDING);

          List<BookingPassenger> passengers = new ArrayList<>();
          if (req.getPassengers() != null) {
               for (PassengerRequest p : req.getPassengers()) {
                    if (p == null || p.getFullName() == null || p.getFullName().isBlank()
                              || p.getType() == null) {
                         continue;
                    }
                    BookingPassenger bp = new BookingPassenger();
                    bp.setBooking(booking);
                    bp.setFullName(p.getFullName().trim());
                    bp.setDob(p.getDob());
                    bp.setPassengerType(p.getType() == PassengerType.CHILD ? PassengerType.CHILD : PassengerType.ADULT);
                    passengers.add(bp);
               }
          }
          booking.setPassengers(passengers);

          return bookingRepository.save(booking);
     }

     @Transactional(readOnly = true)
     public List<Booking> getMyBookings() {
          UserAccount user = getCurrentUser();
          if (user == null || user.getId() == null) {
               return List.of();
          }
          return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
     }

     private String generateBookingCode(LocalDate date) {
          String datePart = date != null ? date.format(DateTimeFormatter.BASIC_ISO_DATE) : "";
          String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
          return "BK" + datePart + rand;
     }

     private UserAccount getCurrentUser() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
               return null;
          }

          String principal = auth.getName();
          if (principal != null && !principal.isBlank()) {
               UserAccount byEmail = userAccountRepository.findByEmail(principal).orElse(null);
               if (byEmail != null) {
                    return byEmail;
               }
               try {
                    Long id = Long.parseLong(principal);
                    return userAccountRepository.findById(id).orElse(null);
               } catch (NumberFormatException ignored) {
                    return null;
               }
          }
          return null;
     }
}
