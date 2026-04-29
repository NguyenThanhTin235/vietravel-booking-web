package com.vietravel.booking.web.dto.tour;

public class ItineraryDayDto {
     private Integer dayNo;
     private String titleRoute;
     private String meals;
     private String contentHtml;
     private Integer sortOrder;

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

     public Integer getSortOrder() {
          return sortOrder;
     }

     public void setSortOrder(Integer sortOrder) {
          this.sortOrder = sortOrder;
     }
}
