package com.vietravel.booking.domain.repository.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRedemptionRepository extends JpaRepository<CampaignRedemption, Long> {
     boolean existsByCampaignIdAndUserId(Long campaignId, Long userId);

     long countByCampaignId(Long campaignId);

     long countByCampaignIdAndUserId(Long campaignId, Long userId);
}
