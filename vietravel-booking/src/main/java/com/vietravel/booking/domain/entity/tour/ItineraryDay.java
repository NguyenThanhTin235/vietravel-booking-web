package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

@Entity
@Table(name = "itinerary_days")
public class ItineraryDay {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tour_id", nullable = false)
     private Tour tour;

     @Column(name = "day_no", nullable = false)
     private Integer dayNo;

     @Column(name = "title_route", nullable = false, length = 300)
     private String titleRoute;

     @Column(length = 30)
     private String meals;

     @Lob
     @Column(name = "content_html", nullable = false)
     private String contentHtml;

     @Column(name = "sort_order", nullable = false)
     private Integer sortOrder = 0;

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public Tour getTour() {
          return tour;
     }

     public void setTour(Tour tour) {
          this.tour = tour;
     }

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
