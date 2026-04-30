package com.vietravel.booking.domain.entity.promotion;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.booking.Booking;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_redemptions")
public class CampaignRedemption {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "campaign_id", nullable = false)
     private Campaign campaign;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private UserAccount user;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "booking_id", nullable = false)
     private Booking booking;

     @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
     private BigDecimal discountAmount = BigDecimal.ZERO;

     @Column(name = "used_at", nullable = false)
     private LocalDateTime usedAt;

     @PrePersist
     public void prePersist() {
          if (usedAt == null) {
               usedAt = LocalDateTime.now();
          }
          if (discountAmount == null) {
               discountAmount = BigDecimal.ZERO;
          }
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public Campaign getCampaign() {
          return campaign;
     }

     public void setCampaign(Campaign campaign) {
          this.campaign = campaign;
     }

     public UserAccount getUser() {
          return user;
     }

     public void setUser(UserAccount user) {
          this.user = user;
     }

     public Booking getBooking() {
          return booking;
     }

     public void setBooking(Booking booking) {
          this.booking = booking;
     }

     public BigDecimal getDiscountAmount() {
          return discountAmount;
     }

     public void setDiscountAmount(BigDecimal discountAmount) {
          this.discountAmount = discountAmount;
     }

     public LocalDateTime getUsedAt() {
          return usedAt;
     }

     public void setUsedAt(LocalDateTime usedAt) {
          this.usedAt = usedAt;
     }
}
