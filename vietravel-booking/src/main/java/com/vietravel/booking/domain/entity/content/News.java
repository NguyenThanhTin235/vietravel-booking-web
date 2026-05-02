package com.vietravel.booking.domain.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
public class News {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false, length = 300)
     private String title;

     @Column(nullable = false, unique = true, length = 350)
     private String slug;

     @Column(length = 600)
     private String thumbnail;

     @Column(columnDefinition = "TEXT")
     private String summary;

     @Column(name = "content_html", columnDefinition = "MEDIUMTEXT", nullable = false)
     private String contentHtml;

     @Column(name = "view_count", nullable = false)
     private Integer viewCount = 0;

     @Column(name = "is_featured", nullable = false)
     private Boolean isFeatured = false;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private NewsStatus status = NewsStatus.PUBLISHED;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at", nullable = false)
     private LocalDateTime updatedAt;

     @PrePersist
     public void prePersist() {
          LocalDateTime now = LocalDateTime.now();
          if (createdAt == null) {
               createdAt = now;
          }
          if (updatedAt == null) {
               updatedAt = now;
          }
          if (viewCount == null) {
               viewCount = 0;
          }
          if (isFeatured == null) {
               isFeatured = false;
          }
          if (status == null) {
               status = NewsStatus.PUBLISHED;
          }
     }

     @PreUpdate
     public void preUpdate() {
          updatedAt = LocalDateTime.now();
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getTitle() {
          return title;
     }

     public void setTitle(String title) {
          this.title = title;
     }

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public String getThumbnail() {
          return thumbnail;
     }

     public void setThumbnail(String thumbnail) {
          this.thumbnail = thumbnail;
     }

     public String getSummary() {
          return summary;
     }

     public void setSummary(String summary) {
          this.summary = summary;
     }

     public String getContentHtml() {
          return contentHtml;
     }

     public void setContentHtml(String contentHtml) {
          this.contentHtml = contentHtml;
     }

     public Integer getViewCount() {
          return viewCount;
     }

     public void setViewCount(Integer viewCount) {
          this.viewCount = viewCount;
     }

     public Boolean getIsFeatured() {
          return isFeatured;
     }

     public void setIsFeatured(Boolean isFeatured) {
          this.isFeatured = isFeatured;
     }

     public NewsStatus getStatus() {
          return status;
     }

     public void setStatus(NewsStatus status) {
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
