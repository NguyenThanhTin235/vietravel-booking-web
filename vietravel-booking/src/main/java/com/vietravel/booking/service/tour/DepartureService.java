package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.DepartureStatus;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.tour.DepartureAdminResponse;
import com.vietravel.booking.web.dto.tour.DepartureUpsertRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class DepartureService {

     private final DepartureRepository departureRepository;
     private final TourRepository tourRepository;

     public DepartureService(
               DepartureRepository departureRepository,
               TourRepository tourRepository) {
          this.departureRepository = departureRepository;
          this.tourRepository = tourRepository;
     }

     @Transactional(readOnly = true)
     public List<DepartureAdminResponse> listCalendar(Long tourId, int year, int month) {
          YearMonth ym = YearMonth.of(year, month);
          LocalDate from = ym.atDay(1);
          LocalDate to = ym.atEndOfMonth();
          return departureRepository.findForCalendar(tourId, from, to)
                    .stream()
                    .map(this::toAdminRes)
                    .toList();
     }

     @SuppressWarnings("null")
     @Transactional
     public DepartureAdminResponse create(DepartureUpsertRequest req) {
          validate(req, null);
          Long tourId = Objects.requireNonNull(req.getTourId(), "tourId");
          Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
          if (departureRepository.existsByTourIdAndStartDateAndStartLocation(
                    req.getTourId(), req.getStartDate(), req.getStartLocation())) {
               throw new RuntimeException("Lịch khởi hành đã tồn tại cho ngày và điểm khởi hành này");
          }

          Departure d = new Departure();
          apply(d, req, tour);
          Departure saved = Objects.requireNonNull(departureRepository.save(d), "savedDeparture");
          return toAdminRes(saved);
     }

     @SuppressWarnings("null")
     @Transactional
     public DepartureAdminResponse update(Long id, DepartureUpsertRequest req) {
          Objects.requireNonNull(id, "id");
          validate(req, id);
          Departure d = departureRepository.findDetailById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khởi hành"));

          Long tourId = Objects.requireNonNull(req.getTourId(), "tourId");
          Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
          if (departureRepository.existsByTourIdAndStartDateAndStartLocationAndIdNot(
                    req.getTourId(), req.getStartDate(), req.getStartLocation(), id)) {
               throw new RuntimeException("Lịch khởi hành đã tồn tại cho ngày và điểm khởi hành này");
          }

          apply(d, req, tour);
          Departure saved = Objects.requireNonNull(departureRepository.save(d), "savedDeparture");
          return toAdminRes(saved);
     }

     @Transactional
     public void delete(Long id) {
          Objects.requireNonNull(id, "id");
          Departure d = departureRepository.findDetailById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khởi hành"));

          // Không cho xóa ngày khởi hành đã qua ngày khởi hành
          if (d.getStartDate() != null && d.getStartDate().isBefore(java.time.LocalDate.now())) {
               throw new RuntimeException("Không thể xóa lịch khởi hành đã qua ngày khởi hành");
          }
          Integer capacity = d.getCapacity();
          Integer available = d.getAvailable();
          if (capacity == null || available == null || !available.equals(capacity)) {
               throw new RuntimeException("Không thể xóa lịch trình vì đã có người đặt");
          }
          departureRepository.deleteById(id);
     }

     private void validate(DepartureUpsertRequest req, Long id) {
          if (req == null) {
               throw new RuntimeException("Dữ liệu không hợp lệ");
          }
          if (req.getTourId() == null) {
               throw new RuntimeException("Tour không được rỗng");
          }
          if (req.getStartDate() == null) {
               throw new RuntimeException("Ngày khởi hành không được rỗng");
          }
          // Không cho thêm ngày khởi hành bé hơn ngày hiện tại
          if (req.getStartDate().isBefore(java.time.LocalDate.now())) {
               throw new RuntimeException("Không thể thêm ngày khởi hành nhỏ hơn ngày hiện tại");
          }
          if (req.getStartLocation() == null) {
               throw new RuntimeException("Điểm khởi hành không được rỗng");
          }
          if (req.getCapacity() == null || req.getCapacity() <= 0) {
               throw new RuntimeException("Số chỗ phải lớn hơn 0");
          }
          if (req.getAvailable() == null) {
               req.setAvailable(req.getCapacity());
          } else if (req.getAvailable() < 0) {
               throw new RuntimeException("Số chỗ còn lại không hợp lệ");
          } else if (req.getAvailable() > req.getCapacity()) {
               throw new RuntimeException("Số chỗ còn lại không thể lớn hơn tổng chỗ");
          }
          if (req.getStatus() == null) {
               req.setStatus(DepartureStatus.OPEN);
          }
     }

     private void apply(Departure d, DepartureUpsertRequest req, Tour tour) {
          d.setTour(tour);
          d.setStartDate(req.getStartDate());
          d.setStartLocation(req.getStartLocation());
          d.setCapacity(req.getCapacity());
          d.setAvailable(req.getAvailable() != null ? req.getAvailable() : req.getCapacity());
          d.setPriceAdult(req.getPriceAdult());
          d.setPriceChild(req.getPriceChild());
          d.setStatus(req.getStatus() == null ? DepartureStatus.OPEN : req.getStatus());
     }

     private DepartureAdminResponse toAdminRes(Departure d) {
          DepartureAdminResponse r = new DepartureAdminResponse();
          r.setId(d.getId());
          r.setTourId(d.getTour() != null ? d.getTour().getId() : null);
          r.setTourTitle(d.getTour() != null ? d.getTour().getTitle() : null);
          r.setDurationDays(d.getTour() != null ? d.getTour().getDurationDays() : null);
          r.setDurationNights(d.getTour() != null ? d.getTour().getDurationNights() : null);
          r.setBasePrice(d.getTour() != null ? d.getTour().getBasePrice() : null);
          r.setStartDate(d.getStartDate());
          r.setCapacity(d.getCapacity());
          Integer booked = d.getId() == null ? 0 : departureRepository.sumBookedByDepartureId(d.getId());
          Integer capacity = d.getCapacity();
          Integer remaining = (capacity == null) ? null : Math.max(capacity - (booked == null ? 0 : booked), 0);
          r.setAvailable(remaining);
          r.setPriceAdult(d.getPriceAdult());
          r.setPriceChild(d.getPriceChild());
          r.setStartLocation(d.getStartLocation());
          r.setStartLocationName(d.getStartLocation() != null ? d.getStartLocation().getLabel() : null);

          // Logic trạng thái động
          LocalDate today = LocalDate.now();
          LocalDate startDate = d.getStartDate();
          boolean isCompleted = (startDate != null && startDate.isBefore(today));
          r.setCompleted(isCompleted);
          if (isCompleted) {
               r.setStatus(DepartureStatus.CLOSED); // Hoặc tạo thêm status DONE nếu muốn
          } else if (capacity != null && booked != null && booked >= capacity) {
               r.setStatus(DepartureStatus.CLOSED); // Đủ số lượng: Đóng
          } else if (startDate != null && !startDate.isAfter(today.plusDays(1))) {
               r.setStatus(DepartureStatus.CLOSED); // Quá hạn đặt: Đóng
          } else {
               r.setStatus(DepartureStatus.OPEN); // Chưa đủ: Mở
          }
          return r;
     }
}
