package com.vietravel.booking.domain.entity.promotion;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false, length = 200)
     private String name;

     @Column(nullable = false, unique = true, length = 250)
     private String slug;

     @Column(columnDefinition = "TEXT")
     private String description;

     @Column(name = "banner_url", length = 600)
     private String bannerUrl;

     @Column(name = "view_count", nullable = false)
     private Integer viewCount = 0;

     @Column(name = "start_at", nullable = false)
     private LocalDateTime startAt;

     @Column(name = "end_at", nullable = false)
     private LocalDateTime endAt;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private CampaignStatus status = CampaignStatus.SCHEDULE;

     @Enumerated(EnumType.STRING)
     @Column(name = "discount_type", nullable = false)
     private CampaignDiscountType discountType;

     @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
     private BigDecimal discountValue = BigDecimal.ZERO;

     @Column(nullable = false, unique = true, length = 50)
     private String code;

     @Column(name = "min_order", nullable = false, precision = 12, scale = 2)
     private BigDecimal minOrder = BigDecimal.ZERO;

     @Column(name = "max_discount", precision = 12, scale = 2)
     private BigDecimal maxDiscount;

     @Column(name = "usage_limit", nullable = false)
     private Integer usageLimit = 0;

     @Column(name = "used_count", nullable = false)
     private Integer usedCount = 0;

     @Column(name = "per_user_limit", nullable = false)
     private Integer perUserLimit = 1;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at", nullable = false)
     private LocalDateTime updatedAt;

     @PrePersist
     public void prePersist() {
          LocalDateTime now = LocalDateTime.now();
          if (createdAt == null) {
               createdAt = now;
          }
          if (updatedAt == null) {
               updatedAt = now;
          }
          if (viewCount == null) {
               viewCount = 0;
          }
          if (status == null) {
               status = CampaignStatus.SCHEDULE;
          }
          if (discountValue == null) {
               discountValue = BigDecimal.ZERO;
          }
          if (minOrder == null) {
               minOrder = BigDecimal.ZERO;
          }
          if (usageLimit == null) {
               usageLimit = 0;
          }
          if (usedCount == null) {
               usedCount = 0;
          }
          if (perUserLimit == null) {
               perUserLimit = 1;
          }
     }

     @PreUpdate
     public void preUpdate() {
          updatedAt = LocalDateTime.now();
     }

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

     public LocalDateTime getUpdatedAt() {
          return updatedAt;
     }
}
