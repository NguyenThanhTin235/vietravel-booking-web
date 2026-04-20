package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.util.List;

public class TourAdminDetailResponse {
     private Long id;
     private String code;
     private String title;
     private String slug;
     private Long tourLineId;
     private Long transportModeId;
     private Integer durationDays;
     private Integer durationNights;
     private Long startLocationId;
     private String startLocationName;
     private BigDecimal basePrice;
     private String summary;
     private String overviewHtml;
     private String additionalInfoHtml;
     private String notesHtml;
     private Boolean isActive;

     private List<Long> categoryIds;
     private List<Long> destinationIds;
     private List<String> destinationNames;
     private List<TourImageDto> images;
     private List<ItineraryDayDto> itineraryDays;

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

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public Long getTourLineId() {
          return tourLineId;
     }

     public void setTourLineId(Long tourLineId) {
          this.tourLineId = tourLineId;
     }

     public Long getTransportModeId() {
          return transportModeId;
     }

     public void setTransportModeId(Long transportModeId) {
          this.transportModeId = transportModeId;
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

     public Long getStartLocationId() {
          return startLocationId;
     }

     public void setStartLocationId(Long startLocationId) {
          this.startLocationId = startLocationId;
     }

     public String getStartLocationName() {
          return startLocationName;
     }

     public void setStartLocationName(String startLocationName) {
          this.startLocationName = startLocationName;
     }

     public BigDecimal getBasePrice() {
          return basePrice;
     }

     public void setBasePrice(BigDecimal basePrice) {
          this.basePrice = basePrice;
     }

     public String getSummary() {
          return summary;
     }

     public void setSummary(String summary) {
          this.summary = summary;
     }

     public String getOverviewHtml() {
          return overviewHtml;
     }

     public void setOverviewHtml(String overviewHtml) {
          this.overviewHtml = overviewHtml;
     }

     public String getAdditionalInfoHtml() {
          return additionalInfoHtml;
     }

     public void setAdditionalInfoHtml(String additionalInfoHtml) {
          this.additionalInfoHtml = additionalInfoHtml;
     }

     public String getNotesHtml() {
          return notesHtml;
     }

     public void setNotesHtml(String notesHtml) {
          this.notesHtml = notesHtml;
     }

     public Boolean getIsActive() {
          return isActive;
     }

     public void setIsActive(Boolean isActive) {
          this.isActive = isActive;
     }

     public List<Long> getCategoryIds() {
          return categoryIds;
     }

     public void setCategoryIds(List<Long> categoryIds) {
          this.categoryIds = categoryIds;
     }

     public List<Long> getDestinationIds() {
          return destinationIds;
     }

     public void setDestinationIds(List<Long> destinationIds) {
          this.destinationIds = destinationIds;
     }

     public List<String> getDestinationNames() {
          return destinationNames;
     }

     public void setDestinationNames(List<String> destinationNames) {
          this.destinationNames = destinationNames;
     }

     public List<TourImageDto> getImages() {
          return images;
     }

     public void setImages(List<TourImageDto> images) {
          this.images = images;
     }

     public List<ItineraryDayDto> getItineraryDays() {
          return itineraryDays;
     }

     public void setItineraryDays(List<ItineraryDayDto> itineraryDays) {
          this.itineraryDays = itineraryDays;
     }
}
