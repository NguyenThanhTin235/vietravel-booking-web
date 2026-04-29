package com.vietravel.booking.domain.entity.support;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_inquiries")
public class ContactInquiry {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private UserAccount user;

     @Column(name = "info_type", nullable = false, length = 50)
     private String infoType;

     @Column(name = "full_name", nullable = false, length = 200)
     private String fullName;

     @Column(nullable = false, length = 255)
     private String email;

     @Column(nullable = false, length = 30)
     private String phone;

     @Column(name = "company_name", length = 200)
     private String companyName;

     @Column(name = "guest_count")
     private Integer guestCount;

     @Column(length = 300)
     private String address;

     @Column(nullable = false, length = 300)
     private String subject;

     @Column(columnDefinition = "TEXT", nullable = false)
     private String content;

     @Column(name = "reply_history", columnDefinition = "TEXT")
     private String replyHistory;

     @Column(name = "reply_count", nullable = false)
     private Integer replyCount = 0;

     @Column(name = "last_replied_at")
     private LocalDateTime lastRepliedAt;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false, length = 20)
     private ContactInquiryStatus status = ContactInquiryStatus.NEW;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "handled_by")
     private UserAccount handledBy;

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
          if (status == null) {
               status = ContactInquiryStatus.NEW;
          }
          if (replyCount == null) {
               replyCount = 0;
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

     public UserAccount getUser() {
          return user;
     }

     public void setUser(UserAccount user) {
          this.user = user;
     }

     public String getInfoType() {
          return infoType;
     }

     public void setInfoType(String infoType) {
          this.infoType = infoType;
     }

     public String getFullName() {
          return fullName;
     }

     public void setFullName(String fullName) {
          this.fullName = fullName;
     }

     public String getEmail() {
          return email;
     }

     public void setEmail(String email) {
          this.email = email;
     }

     public String getPhone() {
          return phone;
     }

     public void setPhone(String phone) {
          this.phone = phone;
     }

     public String getCompanyName() {
          return companyName;
     }

     public void setCompanyName(String companyName) {
          this.companyName = companyName;
     }

     public Integer getGuestCount() {
          return guestCount;
     }

     public void setGuestCount(Integer guestCount) {
          this.guestCount = guestCount;
     }

     public String getAddress() {
          return address;
     }

     public void setAddress(String address) {
          this.address = address;
     }

     public String getSubject() {
          return subject;
     }

     public void setSubject(String subject) {
          this.subject = subject;
     }

     public String getContent() {
          return content;
     }

     public void setContent(String content) {
          this.content = content;
     }

     public String getReplyHistory() {
          return replyHistory;
     }

     public void setReplyHistory(String replyHistory) {
          this.replyHistory = replyHistory;
     }

     public Integer getReplyCount() {
          return replyCount;
     }

     public void setReplyCount(Integer replyCount) {
          this.replyCount = replyCount;
     }

     public LocalDateTime getLastRepliedAt() {
          return lastRepliedAt;
     }

     public void setLastRepliedAt(LocalDateTime lastRepliedAt) {
          this.lastRepliedAt = lastRepliedAt;
     }

     public ContactInquiryStatus getStatus() {
          return status;
     }

     public void setStatus(ContactInquiryStatus status) {
          this.status = status;
     }

     public UserAccount getHandledBy() {
          return handledBy;
     }

     public void setHandledBy(UserAccount handledBy) {
          this.handledBy = handledBy;
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
