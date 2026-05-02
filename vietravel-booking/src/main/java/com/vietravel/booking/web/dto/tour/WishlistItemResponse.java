package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistItemResponse {

     private Long tourId;
     private String slug;
     private String title;
     private BigDecimal basePrice;
     private String thumbnailUrl;
     private LocalDateTime createdAt;

     public Long getTourId() {
          return tourId;
     }

     public void setTourId(Long tourId) {
          this.tourId = tourId;
     }

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public String getTitle() {
          return title;
     }

     public void setTitle(String title) {
          this.title = title;
     }

     public BigDecimal getBasePrice() {
          return basePrice;
     }

     public void setBasePrice(BigDecimal basePrice) {
          this.basePrice = basePrice;
     }

     public String getThumbnailUrl() {
          return thumbnailUrl;
     }

     public void setThumbnailUrl(String thumbnailUrl) {
          this.thumbnailUrl = thumbnailUrl;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }
}
