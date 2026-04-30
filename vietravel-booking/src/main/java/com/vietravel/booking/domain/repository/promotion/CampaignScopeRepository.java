package com.vietravel.booking.domain.repository.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignScopeRepository extends JpaRepository<CampaignScope, Long> {
     List<CampaignScope> findByCampaignId(Long campaignId);

     void deleteByCampaignId(Long campaignId);
}
