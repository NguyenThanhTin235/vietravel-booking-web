package com.vietravel.booking.domain.entity.promotion;

import jakarta.persistence.*;

@Entity
@Table(name = "campaign_scopes")
public class CampaignScope {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "campaign_id", nullable = false)
     private Campaign campaign;

     @Enumerated(EnumType.STRING)
     @Column(name = "scope_type", nullable = false)
     private CampaignScopeType scopeType;

     @Column(name = "ref_id")
     private Long refId;

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public Campaign getCampaign() {
          return campaign;
     }

     public void setCampaign(Campaign campaign) {
          this.campaign = campaign;
     }

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
