package com.vietravel.booking.domain.repository.promotion;

import com.vietravel.booking.domain.entity.promotion.Campaign;
import com.vietravel.booking.domain.entity.promotion.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
     Optional<Campaign> findBySlug(String slug);

     Optional<Campaign> findByCode(String code);

     boolean existsBySlug(String slug);

     boolean existsByCode(String code);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("select c from Campaign c where c.code = :code")
     Optional<Campaign> findForUpdateByCode(@Param("code") String code);

     @Query("select c from Campaign c where c.status = :status order by c.createdAt desc")
     List<Campaign> findByStatus(@Param("status") CampaignStatus status);

     @Query("select c from Campaign c where c.status = 'ACTIVE' and c.startAt <= :now and c.endAt >= :now order by c.startAt desc")
     List<Campaign> findForPublic(@Param("now") LocalDateTime now);
}
