package com.vietravel.booking.domain.entity.booking;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_passengers")
public class BookingPassenger {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "booking_id", nullable = false)
     private Booking booking;

     @Column(name = "full_name", nullable = false, length = 200)
     private String fullName;

     @Column(name = "dob")
     private LocalDate dob;

     @Enumerated(EnumType.STRING)
     @Column(name = "passenger_type", nullable = false)
     private PassengerType passengerType;

     @Column(name = "id_number", length = 50)
     private String idNumber;

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

     public String getFullName() {
          return fullName;
     }

     public void setFullName(String fullName) {
          this.fullName = fullName;
     }

     public LocalDate getDob() {
          return dob;
     }

     public void setDob(LocalDate dob) {
          this.dob = dob;
     }

     public PassengerType getPassengerType() {
          return passengerType;
     }

     public void setPassengerType(PassengerType passengerType) {
          this.passengerType = passengerType;
     }

     public String getIdNumber() {
          return idNumber;
     }

     public void setIdNumber(String idNumber) {
          this.idNumber = idNumber;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }
}
