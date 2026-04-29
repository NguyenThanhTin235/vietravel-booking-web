package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {

     @Modifying
     @Query("delete from ItineraryDay d where d.tour.id = :tourId")
     void deleteByTourId(@Param("tourId") Long tourId);
}
