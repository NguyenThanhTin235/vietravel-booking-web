package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

@Entity
@Table(name = "tour_images")
public class TourImage {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tour_id", nullable = false)
     private Tour tour;

     @Column(name = "image_url", nullable = false, length = 600)
     private String imageUrl;

     @Column(name = "is_thumbnail", nullable = false)
     private Boolean isThumbnail = false;

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

     public String getImageUrl() {
          return imageUrl;
     }

     public void setImageUrl(String imageUrl) {
          this.imageUrl = imageUrl;
     }

     public Boolean getIsThumbnail() {
          return isThumbnail;
     }

     public void setIsThumbnail(Boolean isThumbnail) {
          this.isThumbnail = isThumbnail;
     }

     public Integer getSortOrder() {
          return sortOrder;
     }

     public void setSortOrder(Integer sortOrder) {
          this.sortOrder = sortOrder;
     }
}
