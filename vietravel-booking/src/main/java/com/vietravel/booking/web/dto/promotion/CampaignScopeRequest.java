package com.vietravel.booking.web.dto.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignScopeType;

public class CampaignScopeRequest {
     private CampaignScopeType scopeType;
     private Long refId;

     public CampaignScopeType getScopeType() {
          return scopeType;
     }

     public void setScopeType(CampaignScopeType scopeType) {
          this.scopeType = scopeType;
     }

     public Long getRefId() {
          return refId;
     }

     public void setRefId(Long refId) {
          this.refId = refId;
     }
}
