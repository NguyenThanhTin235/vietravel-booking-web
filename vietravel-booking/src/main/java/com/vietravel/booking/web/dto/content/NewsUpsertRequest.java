package com.vietravel.booking.web.dto.content;

import com.vietravel.booking.domain.entity.content.NewsStatus;

public class NewsUpsertRequest {
     private String title;
     private String slug;
     private String thumbnail;
     private String summary;
     private String contentHtml;
     private Boolean isFeatured;
     private NewsStatus status;

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
}
