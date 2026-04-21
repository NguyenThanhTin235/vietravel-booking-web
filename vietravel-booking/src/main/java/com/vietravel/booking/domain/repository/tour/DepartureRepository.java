package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.StartLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartureRepository extends JpaRepository<Departure, Long> {

  @EntityGraph(attributePaths = { "tour", "startLocation" })
  @Query("""
      select d
      from Departure d
      where (:tourId is null or d.tour.id = :tourId)
        and d.startDate between :from and :to
      order by d.startDate asc, d.id asc
      """)
  List<Departure> findForCalendar(
      @Param("tourId") Long tourId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  @EntityGraph(attributePaths = { "tour", "startLocation" })
  @Query("select d from Departure d where d.id = :id")
  Optional<Departure> findDetailById(@Param("id") Long id);

  boolean existsByTourIdAndStartDateAndStartLocation(Long tourId, LocalDate startDate, StartLocation startLocation);

  boolean existsByTourIdAndStartDateAndStartLocationAndIdNot(
      Long tourId,
      LocalDate startDate,
      StartLocation startLocation,
      Long id);

  @Query(value = """
      select coalesce(sum(b.total_adult + b.total_child), 0)
      from bookings b
      where b.departure_id = :id
        and b.status <> 'CANCELED'
      """, nativeQuery = true)
  Integer sumBookedByDepartureId(@Param("id") Long id);
}
