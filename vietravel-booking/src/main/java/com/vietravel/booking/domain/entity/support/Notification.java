package com.vietravel.booking.domain.entity.support;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private UserAccount user;

     @Column(nullable = false, length = 200)
     private String title;

     @Column(nullable = false, length = 800)
     private String message;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false, length = 20)
     private NotificationType type = NotificationType.INFO;

     @Column(length = 500)
     private String link;

     @Column(name = "is_read", nullable = false)
     private boolean read = false;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @Column(name = "read_at")
     private LocalDateTime readAt;

     @PrePersist
     void prePersist() {
          if (createdAt == null) {
               createdAt = LocalDateTime.now();
          }
          if (type == null) {
               type = NotificationType.INFO;
          }
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public UserAccount getUser() {
          return user;
     }

     public void setUser(UserAccount user) {
          this.user = user;
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

     public LocalDateTime getReadAt() {
          return readAt;
     }

     public void setReadAt(LocalDateTime readAt) {
          this.readAt = readAt;
     }
}
