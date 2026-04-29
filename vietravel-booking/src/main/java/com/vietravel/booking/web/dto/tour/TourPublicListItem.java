package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TourPublicListItem {
     private Long id;
     private String slug;
     private String title;
     private String code;
     private Integer durationDays;
     private Integer durationNights;
     private BigDecimal basePrice;
     private String tourLineName;
     private String startLocationName;
     private String transportModeName;
     private String thumbnailUrl;
     private List<LocalDate> departureDates = new ArrayList<>();

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
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

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
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

     public String getTourLineName() {
          return tourLineName;
     }

     public void setTourLineName(String tourLineName) {
          this.tourLineName = tourLineName;
     }

     public String getStartLocationName() {
          return startLocationName;
     }

     public void setStartLocationName(String startLocationName) {
          this.startLocationName = startLocationName;
     }

     public String getTransportModeName() {
          return transportModeName;
     }

     public void setTransportModeName(String transportModeName) {
          this.transportModeName = transportModeName;
     }

     public String getThumbnailUrl() {
          return thumbnailUrl;
     }

     public void setThumbnailUrl(String thumbnailUrl) {
          this.thumbnailUrl = thumbnailUrl;
     }

     public List<LocalDate> getDepartureDates() {
          return departureDates;
     }

     public void setDepartureDates(List<LocalDate> departureDates) {
          this.departureDates = departureDates;
     }
}
