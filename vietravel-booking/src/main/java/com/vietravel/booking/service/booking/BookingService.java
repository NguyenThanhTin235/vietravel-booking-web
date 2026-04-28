package com.vietravel.booking.service.booking;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.BookingPassenger;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import com.vietravel.booking.domain.entity.booking.PassengerType;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;
import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourImage;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.booking.BookingRepository;
import com.vietravel.booking.domain.repository.booking.PaymentRepository;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest.PassengerRequest;
import com.vietravel.booking.web.dto.booking.BookingHistoryView;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
     private final PaymentRepository paymentRepository;

     public BookingService(BookingRepository bookingRepository,
               TourRepository tourRepository,
               DepartureRepository departureRepository,
               UserAccountRepository userAccountRepository,
               PaymentRepository paymentRepository) {
          this.bookingRepository = bookingRepository;
          this.tourRepository = tourRepository;
          this.departureRepository = departureRepository;
          this.userAccountRepository = userAccountRepository;
          this.paymentRepository = paymentRepository;
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
                    bp.setPassengerType(resolvePassengerType(p.getType()));
                    passengers.add(bp);
               }
          }
          booking.setPassengers(passengers);

          return bookingRepository.save(booking);
     }

     @Transactional
     public Booking createBookingByStaff(BookingCreateRequest req) {
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
          booking.setUser(null);
          booking.setCreatedByStaff(getCurrentUser());
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
                    bp.setPassengerType(resolvePassengerType(p.getType()));
                    passengers.add(bp);
               }
          }
          booking.setPassengers(passengers);

          return bookingRepository.save(booking);
     }

     @Transactional(readOnly = true)
     public List<BookingHistoryView> getMyBookings(BookingStatus bookingStatus, PaymentStatus paymentStatus) {
          UserAccount user = getCurrentUser();
          if (user == null || user.getId() == null) {
               return List.of();
          }
          List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
          List<BookingHistoryView> results = new ArrayList<>();
          for (Booking booking : bookings) {
               if (bookingStatus != null && booking.getStatus() != bookingStatus) {
                    continue;
               }
               PaymentStatus resolvedPayment = resolvePaymentStatus(booking);
               if (paymentStatus != null && resolvedPayment != paymentStatus) {
                    continue;
               }
               String thumbnail = resolveThumbnail(booking);
               results.add(new BookingHistoryView(booking, resolvedPayment, canCancel(booking), thumbnail));
          }
          return results;
     }

     @Transactional(readOnly = true)
     public BookingHistoryView getMyBookingDetail(Long id) {
          UserAccount user = getCurrentUser();
          if (user == null || user.getId() == null || id == null) {
               return null;
          }
          Booking booking = bookingRepository.findWithDetailsById(id).orElse(null);
          if (booking == null || booking.getUser() == null || !user.getId().equals(booking.getUser().getId())) {
               return null;
          }
          return new BookingHistoryView(booking, resolvePaymentStatus(booking), canCancel(booking),
                    resolveThumbnail(booking));
     }

     @Transactional(readOnly = true)
     public BookingHistoryView getBookingViewById(Long id, boolean includeThumbnail) {
          if (id == null) {
               return null;
          }
          Booking booking = bookingRepository.findWithDetailsById(id).orElse(null);
          if (booking == null) {
               return null;
          }
          String thumbnail = includeThumbnail ? resolveThumbnail(booking) : null;
          return new BookingHistoryView(booking, resolvePaymentStatus(booking), canCancel(booking), thumbnail);
     }

     @Transactional(readOnly = true)
     public List<BookingHistoryView> buildBookingViews(List<Booking> bookings) {
          return buildBookingViews(bookings, true);
     }

     @Transactional(readOnly = true)
     public List<BookingHistoryView> buildBookingViews(List<Booking> bookings, boolean includeThumbnail) {
          if (bookings == null || bookings.isEmpty()) {
               return List.of();
          }
          List<BookingHistoryView> results = new ArrayList<>();
          for (Booking booking : bookings) {
               String thumbnail = includeThumbnail ? resolveThumbnail(booking) : null;
               results.add(
                         new BookingHistoryView(booking, resolvePaymentStatus(booking), canCancel(booking), thumbnail));
          }
          return results;
     }

     @Transactional
     public boolean cancelMyBooking(Long id) {
          UserAccount user = getCurrentUser();
          if (user == null || user.getId() == null || id == null) {
               return false;
          }
          Booking booking = bookingRepository.findWithDetailsById(id).orElse(null);
          if (booking == null || booking.getUser() == null || !user.getId().equals(booking.getUser().getId())) {
               return false;
          }
          if (!canCancel(booking)) {
               return false;
          }
          booking.setStatus(BookingStatus.CANCELED);
          bookingRepository.save(booking);
          return true;
     }

     @Transactional
     public boolean confirmBookingByStaff(Long id) {
          if (id == null) {
               return false;
          }
          Booking booking = bookingRepository.findById(id).orElse(null);
          if (booking == null || booking.getStatus() == null) {
               return false;
          }
          if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.PAID) {
               booking.setStatus(BookingStatus.CONFIRMED);
               bookingRepository.save(booking);
               return true;
          }
          return false;
     }

     @Transactional
     public boolean cancelBookingByStaff(Long id, String reason) {
          if (id == null) {
               return false;
          }
          Booking booking = bookingRepository.findById(id).orElse(null);
          if (booking == null || booking.getStatus() == null) {
               return false;
          }
          if (booking.getStatus() == BookingStatus.CANCELED || booking.getStatus() == BookingStatus.COMPLETED) {
               return false;
          }
          booking.setStatus(BookingStatus.CANCELED);
          if (reason != null && !reason.isBlank()) {
               String staffReason = "Lý do hủy (NV): " + reason.trim();
               String existing = booking.getNote();
               String merged = existing == null || existing.isBlank()
                         ? staffReason
                         : existing + "\n" + staffReason;
               if (merged.length() > 500) {
                    merged = merged.substring(0, 500);
               }
               booking.setNote(merged);
          }
          bookingRepository.save(booking);
          return true;
     }

     private PaymentStatus resolvePaymentStatus(Booking booking) {
          if (booking == null || booking.getId() == null) {
               return PaymentStatus.INIT;
          }
          return paymentRepository.findTopByBookingIdOrderByCreatedAtDesc(booking.getId())
                    .map(p -> p.getStatus() == null ? PaymentStatus.INIT : p.getStatus())
                    .orElse(PaymentStatus.INIT);
     }

     private boolean canCancel(Booking booking) {
          if (booking == null || booking.getDeparture() == null || booking.getDeparture().getStartDate() == null) {
               return false;
          }
          if (booking.getStatus() == BookingStatus.CANCELED
                    || booking.getStatus() == BookingStatus.CANCEL_REQUESTED
                    || booking.getStatus() == BookingStatus.COMPLETED) {
               return false;
          }
          long days = ChronoUnit.DAYS.between(LocalDate.now(), booking.getDeparture().getStartDate());
          return days >= 7;
     }

     private String resolveThumbnail(Booking booking) {
          if (booking == null || booking.getDeparture() == null || booking.getDeparture().getTour() == null) {
               return "/images/login-bg.jpg";
          }
          Tour tour = booking.getDeparture().getTour();
          if (tour.getImages() == null || tour.getImages().isEmpty()) {
               return "/images/login-bg.jpg";
          }
          TourImage first = tour.getImages().get(0);
          return first != null && first.getImageUrl() != null ? first.getImageUrl() : "/images/login-bg.jpg";
     }

     private String generateBookingCode(LocalDate date) {
          String datePart = date != null ? date.format(DateTimeFormatter.BASIC_ISO_DATE) : "";
          String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
          return "BK" + datePart + rand;
     }

     private PassengerType resolvePassengerType(PassengerType type) {
          if (type == null) {
               return PassengerType.ADULT;
          }
          if (type == PassengerType.CHILD) {
               return PassengerType.CHILD;
          }
          return PassengerType.ADULT;
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
