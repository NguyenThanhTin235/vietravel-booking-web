package com.vietravel.booking.web.dto.booking;

import com.vietravel.booking.domain.entity.booking.BookingStatus;

public class BookingCreateResponse {
     private String bookingCode;
     private BookingStatus status;
     private String paymentUrl;

     public BookingCreateResponse() {
     }

     public BookingCreateResponse(String bookingCode, BookingStatus status) {
          this.bookingCode = bookingCode;
          this.status = status;
     }

     public BookingCreateResponse(String bookingCode, BookingStatus status, String paymentUrl) {
          this.bookingCode = bookingCode;
          this.status = status;
          this.paymentUrl = paymentUrl;
     }

     public String getBookingCode() {
          return bookingCode;
     }

     public void setBookingCode(String bookingCode) {
          this.bookingCode = bookingCode;
     }

     public BookingStatus getStatus() {
          return status;
     }

     public void setStatus(BookingStatus status) {
          this.status = status;
     }

     public String getPaymentUrl() {
          return paymentUrl;
     }

     public void setPaymentUrl(String paymentUrl) {
          this.paymentUrl = paymentUrl;
     }
}
