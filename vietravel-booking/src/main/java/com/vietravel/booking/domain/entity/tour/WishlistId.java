package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WishlistId implements Serializable {

     private Long userId;
     private Long tourId;

     public WishlistId() {
     }

     public WishlistId(Long userId, Long tourId) {
          this.userId = userId;
          this.tourId = tourId;
     }

     public Long getUserId() {
          return userId;
     }

     public void setUserId(Long userId) {
          this.userId = userId;
     }

     public Long getTourId() {
          return tourId;
     }

     public void setTourId(Long tourId) {
          this.tourId = tourId;
     }

     @Override
     public boolean equals(Object o) {
          if (this == o)
               return true;
          if (o == null || getClass() != o.getClass())
               return false;
          WishlistId that = (WishlistId) o;
          return Objects.equals(userId, that.userId)
                    && Objects.equals(tourId, that.tourId);
     }

     @Override
     public int hashCode() {
          return Objects.hash(userId, tourId);
     }
}
