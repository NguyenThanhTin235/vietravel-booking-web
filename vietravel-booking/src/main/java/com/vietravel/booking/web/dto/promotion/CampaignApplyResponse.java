package com.vietravel.booking.web.dto.promotion;

import java.math.BigDecimal;

public class CampaignApplyResponse {
     private boolean valid;
     private String message;
     private BigDecimal discountAmount;
     private BigDecimal totalAmount;
     private String campaignName;

     public boolean isValid() {
          return valid;
     }

     public void setValid(boolean valid) {
          this.valid = valid;
     }

     public String getMessage() {
          return message;
     }

     public void setMessage(String message) {
          this.message = message;
     }

     public BigDecimal getDiscountAmount() {
          return discountAmount;
     }

     public void setDiscountAmount(BigDecimal discountAmount) {
          this.discountAmount = discountAmount;
     }

     public BigDecimal getTotalAmount() {
          return totalAmount;
     }

     public void setTotalAmount(BigDecimal totalAmount) {
          this.totalAmount = totalAmount;
     }

     public String getCampaignName() {
          return campaignName;
     }

     public void setCampaignName(String campaignName) {
          this.campaignName = campaignName;
     }
}
