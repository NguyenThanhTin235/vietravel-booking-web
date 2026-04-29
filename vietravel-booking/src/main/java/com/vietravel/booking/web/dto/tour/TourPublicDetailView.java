package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TourPublicDetailView {
     private Long id;
     private String slug;
     private String title;
     private String code;
     private Integer durationDays;
     private Integer durationNights;
     private BigDecimal basePrice;
     private String tourLineName;
     private String transportModeName;
     private String startLocationName;
     private List<String> destinationNames = new ArrayList<>();
     private List<String> categoryNames = new ArrayList<>();
     private String overviewHtml;
     private String additionalInfoHtml;
     private String notesHtml;
     private List<ItineraryDayView> itineraryDays = new ArrayList<>();
     private List<String> imageUrls = new ArrayList<>();

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

     public List<String> getDestinationNames() {
          return destinationNames;
     }

     public void setDestinationNames(List<String> destinationNames) {
          this.destinationNames = destinationNames;
     }

     public List<String> getCategoryNames() {
          return categoryNames;
     }

     public void setCategoryNames(List<String> categoryNames) {
          this.categoryNames = categoryNames;
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

     public List<ItineraryDayView> getItineraryDays() {
          return itineraryDays;
     }

     public void setItineraryDays(List<ItineraryDayView> itineraryDays) {
          this.itineraryDays = itineraryDays;
     }

     public List<String> getImageUrls() {
          return imageUrls;
     }

     public void setImageUrls(List<String> imageUrls) {
          this.imageUrls = imageUrls;
     }

     public static class ItineraryDayView {
          private Integer dayNo;
          private String titleRoute;
          private String meals;
          private String contentHtml;

          public Integer getDayNo() {
               return dayNo;
          }

          public void setDayNo(Integer dayNo) {
               this.dayNo = dayNo;
          }

          public String getTitleRoute() {
               return titleRoute;
          }

          public void setTitleRoute(String titleRoute) {
               this.titleRoute = titleRoute;
          }

          public String getMeals() {
               return meals;
          }

          public void setMeals(String meals) {
               this.meals = meals;
          }

          public String getContentHtml() {
               return contentHtml;
          }

          public void setContentHtml(String contentHtml) {
               this.contentHtml = contentHtml;
          }
     }
}
