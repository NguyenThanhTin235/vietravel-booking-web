package com.vietravel.booking.web.dto.content;

import com.vietravel.booking.domain.entity.content.NewsStatus;

import java.time.LocalDateTime;

public class NewsResponse {
     private Long id;
     private String title;
     private String slug;
     private String thumbnail;
     private String summary;
     private String contentHtml;
     private Integer viewCount;
     private Boolean isFeatured;
     private NewsStatus status;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;

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
