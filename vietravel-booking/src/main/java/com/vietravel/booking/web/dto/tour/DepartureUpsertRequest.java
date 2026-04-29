package com.vietravel.booking.web.dto.tour;

import com.vietravel.booking.domain.entity.tour.DepartureStatus;
import com.vietravel.booking.domain.entity.tour.StartLocation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DepartureUpsertRequest {
     private Long tourId;
     private LocalDate startDate;
     private StartLocation startLocation;
     private Integer capacity;
     private Integer available;
     private BigDecimal priceAdult;
     private BigDecimal priceChild;
     private DepartureStatus status;

     public Long getTourId() {
          return tourId;
     }

     public void setTourId(Long tourId) {
          this.tourId = tourId;
     }

     public LocalDate getStartDate() {
          return startDate;
     }

     public void setStartDate(LocalDate startDate) {
          this.startDate = startDate;
     }

     public StartLocation getStartLocation() {
          return startLocation;
     }

     public void setStartLocation(StartLocation startLocation) {
          this.startLocation = startLocation;
     }

     public Integer getCapacity() {
          return capacity;
     }

     public void setCapacity(Integer capacity) {
          this.capacity = capacity;
     }

     public Integer getAvailable() {
          return available;
     }

     public void setAvailable(Integer available) {
          this.available = available;
     }

     public BigDecimal getPriceAdult() {
          return priceAdult;
     }

     public void setPriceAdult(BigDecimal priceAdult) {
          this.priceAdult = priceAdult;
     }

     public BigDecimal getPriceChild() {
          return priceChild;
     }

     public void setPriceChild(BigDecimal priceChild) {
          this.priceChild = priceChild;
     }

     public DepartureStatus getStatus() {
          return status;
     }

     public void setStatus(DepartureStatus status) {
          this.status = status;
     }
}
