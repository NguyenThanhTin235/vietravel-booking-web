package com.vietravel.booking.domain.entity.support;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
public class Branch {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false, length = 30)
     private String region;

     @Column(nullable = false, length = 250)
     private String name;

     @Column(nullable = false, length = 350)
     private String address;

     @Column(length = 120)
     private String hotline;

     @Column(length = 255)
     private String email;

     @Column(length = 80)
     private String fax;

     @Column(name = "is_active", nullable = false)
     private Boolean isActive = true;

     @Column(name = "sort_order", nullable = false)
     private Integer sortOrder = 0;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @PrePersist
     public void prePersist() {
          if (createdAt == null) {
               createdAt = LocalDateTime.now();
          }
          if (isActive == null) {
               isActive = true;
          }
          if (sortOrder == null) {
               sortOrder = 0;
          }
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getRegion() {
          return region;
     }

     public void setRegion(String region) {
          this.region = region;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getAddress() {
          return address;
     }

     public void setAddress(String address) {
          this.address = address;
     }

     public String getHotline() {
          return hotline;
     }

     public void setHotline(String hotline) {
          this.hotline = hotline;
     }

     public String getEmail() {
          return email;
     }

     public void setEmail(String email) {
          this.email = email;
     }

     public String getFax() {
          return fax;
     }

     public void setFax(String fax) {
          this.fax = fax;
     }

     public Boolean getIsActive() {
          return isActive;
     }

     public void setIsActive(Boolean isActive) {
          this.isActive = isActive;
     }

     public Integer getSortOrder() {
          return sortOrder;
     }

     public void setSortOrder(Integer sortOrder) {
          this.sortOrder = sortOrder;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }
}
