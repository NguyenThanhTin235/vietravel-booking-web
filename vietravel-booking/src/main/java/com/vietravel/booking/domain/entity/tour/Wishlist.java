package com.vietravel.booking.domain.entity.tour;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist")
public class Wishlist {

     @EmbeddedId
     private WishlistId id = new WishlistId();

     @MapsId("userId")
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private UserAccount user;

     @MapsId("tourId")
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tour_id", nullable = false)
     private Tour tour;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     public WishlistId getId() {
          return id;
     }

     public void setId(WishlistId id) {
          this.id = id;
     }

     public UserAccount getUser() {
          return user;
     }

     public void setUser(UserAccount user) {
          this.user = user;
     }

     public Tour getTour() {
          return tour;
     }

     public void setTour(Tour tour) {
          this.tour = tour;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }

     @PrePersist
     public void prePersist() {
          if (createdAt == null) {
               createdAt = LocalDateTime.now();
          }
     }
}
