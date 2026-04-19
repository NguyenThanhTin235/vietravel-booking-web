package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.TourCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TourCategoryRepository extends JpaRepository<TourCategory,Long>{
    Optional<TourCategory> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<TourCategory> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}
