package com.vietravel.booking.web.dto.promotion;

import com.vietravel.booking.domain.entity.promotion.CampaignDiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CampaignPublicDetailView {
     private Long id;
     private String name;
     private String slug;
     private String description;
     private String bannerUrl;
     private CampaignDiscountType discountType;
     private BigDecimal discountValue;
     private String code;
     private BigDecimal minOrder;
     private BigDecimal maxDiscount;
     private Integer usageLimit;
     private Integer usedCount;
     private LocalDateTime startAt;
     private LocalDateTime endAt;
     private Integer viewCount;

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

     public Integer getViewCount() {
          return viewCount;
     }

     public void setViewCount(Integer viewCount) {
          this.viewCount = viewCount;
     }
}
