package com.vietravel.booking.repository.tour;

import com.vietravel.booking.domain.entity.tour.TourLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TourLineRepository extends JpaRepository<TourLine,Long>{

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code,Long id);

    List<TourLine> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<TourLine> findFirstByIsActiveTrueAndMinPriceLessThanEqualAndMaxPriceGreaterThanEqual(BigDecimal price1,BigDecimal price2);
}
