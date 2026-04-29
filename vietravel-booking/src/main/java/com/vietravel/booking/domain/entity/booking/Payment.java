package com.vietravel.booking.domain.entity.booking;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "booking_id", nullable = false)
     private Booking booking;

     @Enumerated(EnumType.STRING)
     @Column(name = "payment_type", nullable = false)
     private PaymentType paymentType;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private PaymentMethod method = PaymentMethod.VNPAY_MOCK;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private PaymentStatus status;

     @Column(nullable = false, precision = 12, scale = 2)
     private BigDecimal amount;

     @Column(name = "txn_ref", nullable = false, length = 60)
     private String txnRef;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @PrePersist
     public void prePersist() {
          if (createdAt == null)
               createdAt = LocalDateTime.now();
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public Booking getBooking() {
          return booking;
     }

     public void setBooking(Booking booking) {
          this.booking = booking;
     }

     public PaymentType getPaymentType() {
          return paymentType;
     }

     public void setPaymentType(PaymentType paymentType) {
          this.paymentType = paymentType;
     }

     public PaymentMethod getMethod() {
          return method;
     }

     public void setMethod(PaymentMethod method) {
          this.method = method;
     }

     public PaymentStatus getStatus() {
          return status;
     }

     public void setStatus(PaymentStatus status) {
          this.status = status;
     }

     public BigDecimal getAmount() {
          return amount;
     }

     public void setAmount(BigDecimal amount) {
          this.amount = amount;
     }

     public String getTxnRef() {
          return txnRef;
     }

     public void setTxnRef(String txnRef) {
          this.txnRef = txnRef;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }
}
