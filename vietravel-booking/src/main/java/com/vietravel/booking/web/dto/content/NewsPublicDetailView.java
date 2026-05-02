package com.vietravel.booking.web.dto.content;

import java.time.LocalDateTime;

public class NewsPublicDetailView {
     private Long id;
     private String title;
     private String slug;
     private String thumbnail;
     private String summary;
     private String contentHtml;
     private LocalDateTime createdAt;
     private Integer viewCount;

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

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }

     public Integer getViewCount() {
          return viewCount;
     }

     public void setViewCount(Integer viewCount) {
          this.viewCount = viewCount;
     }
}
