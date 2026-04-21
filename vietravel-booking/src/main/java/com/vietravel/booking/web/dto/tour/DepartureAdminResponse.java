package com.vietravel.booking.web.dto.tour;

import com.vietravel.booking.domain.entity.tour.DepartureStatus;
import com.vietravel.booking.domain.entity.tour.StartLocation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DepartureAdminResponse {
     private Long id;
     private Long tourId;
     private String tourTitle;
     private Integer durationDays;
     private Integer durationNights;
     private BigDecimal basePrice;
     private LocalDate startDate;
     private Integer capacity;
     private Integer available;
     private BigDecimal priceAdult;
     private BigDecimal priceChild;
     private StartLocation startLocation;
     private String startLocationName;
     private DepartureStatus status;
     private Boolean completed;

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public Long getTourId() {
          return tourId;
     }

     public void setTourId(Long tourId) {
          this.tourId = tourId;
     }

     public String getTourTitle() {
          return tourTitle;
     }

     public void setTourTitle(String tourTitle) {
          this.tourTitle = tourTitle;
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

     public LocalDate getStartDate() {
          return startDate;
     }

     public void setStartDate(LocalDate startDate) {
          this.startDate = startDate;
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

     public StartLocation getStartLocation() {
          return startLocation;
     }

     public void setStartLocation(StartLocation startLocation) {
          this.startLocation = startLocation;
     }

     public String getStartLocationName() {
          return startLocationName;
     }

     public void setStartLocationName(String startLocationName) {
          this.startLocationName = startLocationName;
     }

     public DepartureStatus getStatus() {
          return status;
     }

     public void setStatus(DepartureStatus status) {
          this.status = status;
     }

     public Boolean getCompleted() {
          return completed;
     }

     public void setCompleted(Boolean completed) {
          this.completed = completed;
     }
}
