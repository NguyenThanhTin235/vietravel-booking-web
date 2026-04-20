package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.util.List;

public class TourAdminListResponse {
     private Long id;
     private String code;
     private String title;
     private String thumbnailUrl;
     private Integer durationDays;
     private Integer durationNights;
     private BigDecimal basePrice;
     private Boolean isActive;
     private String tourLineName;
     private String transportModeName;
     private String startLocationName;
     private List<String> categories;
     private List<String> destinationNames;

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
     }

     public String getTitle() {
          return title;
     }

     public void setTitle(String title) {
          this.title = title;
     }

     public String getThumbnailUrl() {
          return thumbnailUrl;
     }

     public void setThumbnailUrl(String thumbnailUrl) {
          this.thumbnailUrl = thumbnailUrl;
     }

     public Integer getDurationDays() {
          return durationDays;
     }

     public void setDurationDays(Integer durationDays) {
          this.durationDays = durationDays;
     }

     public Integer getDurationNights() {
          return durationNights;
     }

     public void setDurationNights(Integer durationNights) {
          this.durationNights = durationNights;
     }

     public BigDecimal getBasePrice() {
          return basePrice;
     }

     public void setBasePrice(BigDecimal basePrice) {
          this.basePrice = basePrice;
     }

     public Boolean getIsActive() {
          return isActive;
     }

     public void setIsActive(Boolean isActive) {
          this.isActive = isActive;
     }

     public String getTourLineName() {
          return tourLineName;
     }

     public void setTourLineName(String tourLineName) {
          this.tourLineName = tourLineName;
     }

     public String getTransportModeName() {
          return transportModeName;
     }

     public void setTransportModeName(String transportModeName) {
          this.transportModeName = transportModeName;
     }

     public String getStartLocationName() {
          return startLocationName;
     }

     public void setStartLocationName(String startLocationName) {
          this.startLocationName = startLocationName;
     }

     public List<String> getCategories() {
          return categories;
     }

     public void setCategories(List<String> categories) {
          this.categories = categories;
     }

     public List<String> getDestinationNames() {
          return destinationNames;
     }

     public void setDestinationNames(List<String> destinationNames) {
          this.destinationNames = destinationNames;
     }
}
