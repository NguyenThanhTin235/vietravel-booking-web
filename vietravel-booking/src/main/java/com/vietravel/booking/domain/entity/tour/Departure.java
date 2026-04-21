package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "departures")
public class Departure {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tour_id", nullable = false)
     private Tour tour;

     @Column(name = "start_date", nullable = false)
     private LocalDate startDate;

     @Column(nullable = false)
     private Integer capacity;

     @Column(nullable = false)
     private Integer available;

     @Column(name = "price_adult", nullable = false, precision = 12, scale = 2)
     private BigDecimal priceAdult;

     @Column(name = "price_child", nullable = false, precision = 12, scale = 2)
     private BigDecimal priceChild;

     @Enumerated(EnumType.STRING)
     @Column(name = "start_location", nullable = false)
     private StartLocation startLocation;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private DepartureStatus status = DepartureStatus.OPEN;

     @Column(name = "created_at", nullable = false, updatable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at", nullable = false)
     private LocalDateTime updatedAt;

     @PrePersist
     protected void onCreate() {
          LocalDateTime now = LocalDateTime.now();
          createdAt = now;
          updatedAt = now;
          if (status == null) {
               status = DepartureStatus.OPEN;
          }
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
          if (status == null) {
               status = DepartureStatus.OPEN;
          }
     }

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

     public DepartureStatus getStatus() {
          return status;
     }

     public void setStatus(DepartureStatus status) {
          this.status = status;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }

     public LocalDateTime getUpdatedAt() {
          return updatedAt;
     }

     public void setUpdatedAt(LocalDateTime updatedAt) {
          this.updatedAt = updatedAt;
     }
}
