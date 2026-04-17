package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.TransportMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportModeRepository extends JpaRepository<TransportMode,Long>{
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code,Long id);
    List<TransportMode> findByIsActiveTrueOrderBySortOrderAsc();
}
