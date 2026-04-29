package com.vietravel.booking.web.dto.support;

import com.vietravel.booking.domain.entity.support.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {
     private Long id;
     private String title;
     private String message;
     private NotificationType type;
     private String link;
     private boolean read;
     private LocalDateTime createdAt;

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

     public String getMessage() {
          return message;
     }

     public void setMessage(String message) {
          this.message = message;
     }

     public NotificationType getType() {
          return type;
     }

     public void setType(NotificationType type) {
          this.type = type;
     }

     public String getLink() {
          return link;
     }

     public void setLink(String link) {
          this.link = link;
     }

     public boolean isRead() {
          return read;
     }

     public void setRead(boolean read) {
          this.read = read;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }
}
