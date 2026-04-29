package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.domain.entity.tour.TourLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TourLineRepository extends JpaRepository<TourLine,Long>{

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code,Long id);

    List<TourLine> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<TourLine> findFirstByIsActiveTrueAndMinPriceLessThanEqualAndMaxPriceGreaterThanEqual(BigDecimal price1,BigDecimal price2);
    @Query("select c from TourCategory c left join fetch c.parent where c.id=:id")
    Optional<TourCategory> findByIdFetchParent(@Param("id")Long id);

    @Query("select c from TourCategory c left join fetch c.parent")
    List<TourCategory> findAllFetchParent();

    @Query("select c from TourCategory c left join fetch c.parent where c.isActive=true order by c.sortOrder asc, c.id asc")
    List<TourCategory> findAllActiveFetchParent();

}
