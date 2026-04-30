package com.vietravel.booking.web.dto.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignScopeType;

public class CampaignScopeResponse {
     private CampaignScopeType scopeType;
     private Long refId;
     private String refName;

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

     public String getRefName() {
          return refName;
     }

     public void setRefName(String refName) {
          this.refName = refName;
     }
}
