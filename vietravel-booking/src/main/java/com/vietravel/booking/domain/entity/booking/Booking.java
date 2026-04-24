package com.vietravel.booking.domain.entity.booking;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.tour.Departure;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "booking_code", nullable = false, unique = true, length = 40)
     private String bookingCode;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private UserAccount user;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "created_by_staff_id")
     private UserAccount createdByStaff;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "departure_id", nullable = false)
     private Departure departure;

     @Column(name = "contact_name", nullable = false, length = 200)
     private String contactName;

     @Column(name = "contact_phone", nullable = false, length = 30)
     private String contactPhone;

     @Column(name = "contact_email", nullable = false, length = 255)
     private String contactEmail;

     @Column(name = "note", length = 500)
     private String note;

     @Column(name = "total_adult", nullable = false)
     private Integer totalAdult = 0;

     @Column(name = "total_child", nullable = false)
     private Integer totalChild = 0;

     @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
     private BigDecimal totalAmount = BigDecimal.ZERO;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private BookingStatus status = BookingStatus.PENDING;

     @Column(name = "created_at", nullable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at", nullable = false)
     private LocalDateTime updatedAt;

     @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<BookingPassenger> passengers = new ArrayList<>();

     @PrePersist
     public void prePersist() {
          LocalDateTime now = LocalDateTime.now();
          if (createdAt == null)
               createdAt = now;
          if (updatedAt == null)
               updatedAt = now;
          if (status == null)
               status = BookingStatus.PENDING;
          if (totalAmount == null)
               totalAmount = BigDecimal.ZERO;
          if (totalAdult == null)
               totalAdult = 0;
          if (totalChild == null)
               totalChild = 0;
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

     public String getBookingCode() {
          return bookingCode;
     }

     public void setBookingCode(String bookingCode) {
          this.bookingCode = bookingCode;
     }

     public UserAccount getUser() {
          return user;
     }

     public void setUser(UserAccount user) {
          this.user = user;
     }

     public UserAccount getCreatedByStaff() {
          return createdByStaff;
     }

     public void setCreatedByStaff(UserAccount createdByStaff) {
          this.createdByStaff = createdByStaff;
     }

     public Departure getDeparture() {
          return departure;
     }

     public void setDeparture(Departure departure) {
          this.departure = departure;
     }

     public String getContactName() {
          return contactName;
     }

     public void setContactName(String contactName) {
          this.contactName = contactName;
     }

     public String getContactPhone() {
          return contactPhone;
     }

     public void setContactPhone(String contactPhone) {
          this.contactPhone = contactPhone;
     }

     public String getContactEmail() {
          return contactEmail;
     }

     public void setContactEmail(String contactEmail) {
          this.contactEmail = contactEmail;
     }

     public String getNote() {
          return note;
     }

     public void setNote(String note) {
          this.note = note;
     }

     public Integer getTotalAdult() {
          return totalAdult;
     }

     public void setTotalAdult(Integer totalAdult) {
          this.totalAdult = totalAdult;
     }

     public Integer getTotalChild() {
          return totalChild;
     }

     public void setTotalChild(Integer totalChild) {
          this.totalChild = totalChild;
     }

     public BigDecimal getTotalAmount() {
          return totalAmount;
     }

     public void setTotalAmount(BigDecimal totalAmount) {
          this.totalAmount = totalAmount;
     }

     public BookingStatus getStatus() {
          return status;
     }

     public void setStatus(BookingStatus status) {
          this.status = status;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public LocalDateTime getUpdatedAt() {
          return updatedAt;
     }

     public List<BookingPassenger> getPassengers() {
          return passengers;
     }

     public void setPassengers(List<BookingPassenger> passengers) {
          this.passengers = passengers;
     }
}
