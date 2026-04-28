package com.vietravel.booking.domain.repository.booking;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
     Optional<Booking> findByBookingCode(String bookingCode);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     Optional<Booking> findWithDepartureTourById(Long id);

     @EntityGraph(attributePaths = { "departure", "departure.tour", "departure.tour.images" })
     List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

     @EntityGraph(attributePaths = {
               "departure",
               "departure.tour",
               "departure.tour.images",
               "departure.tour.tourLine",
               "departure.tour.transportMode",
               "departure.tour.startLocation",
               "passengers"
     })
     Optional<Booking> findWithDetailsById(Long id);

     long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

     long countByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

     long countByStatus(BookingStatus status);

     long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime start, LocalDateTime end);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findTop5ByOrderByCreatedAtDesc();

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findTop5ByStatusInOrderByCreatedAtDesc(List<BookingStatus> statuses);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findTop50ByOrderByCreatedAtDesc();

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     List<Booking> findTop50ByStatusOrderByCreatedAtDesc(BookingStatus status);

     @EntityGraph(attributePaths = { "departure", "departure.tour" })
     @Query("select b from Booking b where b.note is not null and trim(b.note) <> '' order by b.createdAt desc")
     List<Booking> findRecentNotes(Pageable pageable);

     @Query("select count(b) from Booking b where b.note is not null and trim(b.note) <> '' and b.createdAt >= :since")
     long countNotesSince(@Param("since") LocalDateTime since);

     @Query("select coalesce(sum(b.totalAmount), 0) from Booking b where b.createdAt >= :start and b.createdAt < :end")
     BigDecimal sumTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

     @Query("select b.departure.tour.title as title, count(b.id) as total from Booking b group by b.departure.tour.title order by count(b.id) desc")
     List<TourBookingCount> findTopTours(Pageable pageable);

     @Query("select function('date', b.createdAt) as day, count(b.id) as total from Booking b where b.createdAt >= :since group by function('date', b.createdAt) order by function('date', b.createdAt)")
     List<DailyBookingCount> findDailyCountsSince(@Param("since") LocalDateTime since);

     interface TourBookingCount {
          String getTitle();

          long getTotal();
     }

     interface DailyBookingCount {
          LocalDate getDay();

          long getTotal();
     }
}
