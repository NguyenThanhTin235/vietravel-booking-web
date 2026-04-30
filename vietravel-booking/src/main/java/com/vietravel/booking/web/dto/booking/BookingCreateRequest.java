package com.vietravel.booking.web.dto.booking;

import com.vietravel.booking.domain.entity.booking.PassengerType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public class BookingCreateRequest {
     private String slug;

     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
     private LocalDate date;

     private String contactName;
     private String contactPhone;
     private String contactEmail;
     private String note;

     private int totalAdult;
     private int totalChild;

     private String couponCode;

     private List<PassengerRequest> passengers;

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public LocalDate getDate() {
          return date;
     }

     public void setDate(LocalDate date) {
          this.date = date;
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

     public int getTotalAdult() {
          return totalAdult;
     }

     public void setTotalAdult(int totalAdult) {
          this.totalAdult = totalAdult;
     }

     public int getTotalChild() {
          return totalChild;
     }

     public void setTotalChild(int totalChild) {
          this.totalChild = totalChild;
     }

     public String getCouponCode() {
          return couponCode;
     }

     public void setCouponCode(String couponCode) {
          this.couponCode = couponCode;
     }

     public List<PassengerRequest> getPassengers() {
          return passengers;
     }

     public void setPassengers(List<PassengerRequest> passengers) {
          this.passengers = passengers;
     }

     public static class PassengerRequest {
          private String fullName;
          private LocalDate dob;
          private PassengerType type;

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

          public PassengerType getType() {
               return type;
          }

          public void setType(PassengerType type) {
               this.type = type;
          }
     }
}
