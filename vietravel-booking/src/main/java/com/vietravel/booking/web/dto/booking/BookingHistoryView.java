package com.vietravel.booking.web.dto.booking;

import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.PaymentStatus;

public class BookingHistoryView {
     private Booking booking;
     private PaymentStatus paymentStatus;
     private boolean canCancel;
     private String thumbnailUrl;

     public BookingHistoryView() {
     }

     public BookingHistoryView(Booking booking, PaymentStatus paymentStatus, boolean canCancel, String thumbnailUrl) {
          this.booking = booking;
          this.paymentStatus = paymentStatus;
          this.canCancel = canCancel;
          this.thumbnailUrl = thumbnailUrl;
     }

     public Booking getBooking() {
          return booking;
     }

     public void setBooking(Booking booking) {
          this.booking = booking;
     }

     public PaymentStatus getPaymentStatus() {
          return paymentStatus;
     }

     public void setPaymentStatus(PaymentStatus paymentStatus) {
          this.paymentStatus = paymentStatus;
     }

     public boolean isCanCancel() {
          return canCancel;
     }

     public void setCanCancel(boolean canCancel) {
          this.canCancel = canCancel;
     }

     public String getThumbnailUrl() {
          return thumbnailUrl;
     }

     public void setThumbnailUrl(String thumbnailUrl) {
          this.thumbnailUrl = thumbnailUrl;
     }
}
