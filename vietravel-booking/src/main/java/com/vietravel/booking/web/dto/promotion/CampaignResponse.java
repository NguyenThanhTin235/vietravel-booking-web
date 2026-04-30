package com.vietravel.booking.web.dto.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignDiscountType;
import com.vietravel.booking.domain.entity.promotion.CampaignStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CampaignResponse {
     private Long id;
     private String name;
     private String slug;
     private String description;
     private String bannerUrl;
     private Integer viewCount;
     private LocalDateTime startAt;
     private LocalDateTime endAt;
     private CampaignStatus status;
     private CampaignDiscountType discountType;
     private BigDecimal discountValue;
     private String code;
     private BigDecimal minOrder;
     private BigDecimal maxDiscount;
     private Integer usageLimit;
     private Integer usedCount;
     private Integer perUserLimit;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
     private List<CampaignScopeResponse> scopes;

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public String getDescription() {
          return description;
     }

     public void setDescription(String description) {
          this.description = description;
     }

     public String getBannerUrl() {
          return bannerUrl;
     }

     public void setBannerUrl(String bannerUrl) {
          this.bannerUrl = bannerUrl;
     }

     public Integer getViewCount() {
          return viewCount;
     }

     public void setViewCount(Integer viewCount) {
          this.viewCount = viewCount;
     }

     public LocalDateTime getStartAt() {
          return startAt;
     }

     public void setStartAt(LocalDateTime startAt) {
          this.startAt = startAt;
     }

     public LocalDateTime getEndAt() {
          return endAt;
     }

     public void setEndAt(LocalDateTime endAt) {
          this.endAt = endAt;
     }

     public CampaignStatus getStatus() {
          return status;
     }

     public void setStatus(CampaignStatus status) {
          this.status = status;
     }

     public CampaignDiscountType getDiscountType() {
          return discountType;
     }

     public void setDiscountType(CampaignDiscountType discountType) {
          this.discountType = discountType;
     }

     public BigDecimal getDiscountValue() {
          return discountValue;
     }

     public void setDiscountValue(BigDecimal discountValue) {
          this.discountValue = discountValue;
     }

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
     }

     public BigDecimal getMinOrder() {
          return minOrder;
     }

     public void setMinOrder(BigDecimal minOrder) {
          this.minOrder = minOrder;
     }

     public BigDecimal getMaxDiscount() {
          return maxDiscount;
     }

     public void setMaxDiscount(BigDecimal maxDiscount) {
          this.maxDiscount = maxDiscount;
     }

     public Integer getUsageLimit() {
          return usageLimit;
     }

     public void setUsageLimit(Integer usageLimit) {
          this.usageLimit = usageLimit;
     }

     public Integer getUsedCount() {
          return usedCount;
     }

     public void setUsedCount(Integer usedCount) {
          this.usedCount = usedCount;
     }

     public Integer getPerUserLimit() {
          return perUserLimit;
     }

     public void setPerUserLimit(Integer perUserLimit) {
          this.perUserLimit = perUserLimit;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }

     public LocalDateTime getUpdatedAt() {
          return updatedAt;
     }

     public void setUpdatedAt(LocalDateTime updatedAt) {
          this.updatedAt = updatedAt;
     }

     public List<CampaignScopeResponse> getScopes() {
          return scopes;
     }

     public void setScopes(List<CampaignScopeResponse> scopes) {
          this.scopes = scopes;
     }
}
