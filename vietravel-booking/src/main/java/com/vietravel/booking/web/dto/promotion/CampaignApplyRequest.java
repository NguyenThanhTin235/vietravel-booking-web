package com.vietravel.booking.web.dto.promotion;

import java.math.BigDecimal;

public class CampaignApplyRequest {
     private String code;
     private String tourSlug;
     private BigDecimal totalAmount;

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
     }

     public String getTourSlug() {
          return tourSlug;
     }

     public void setTourSlug(String tourSlug) {
          this.tourSlug = tourSlug;
     }

     public BigDecimal getTotalAmount() {
          return totalAmount;
     }

     public void setTotalAmount(BigDecimal totalAmount) {
          this.totalAmount = totalAmount;
     }
}
