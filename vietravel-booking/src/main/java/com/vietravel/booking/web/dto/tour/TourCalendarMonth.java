package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TourCalendarMonth {
     private int year;
     private int month;
     private String label;
     private List<CalendarDay> days = new ArrayList<>();

     public int getYear() {
          return year;
     }

     public void setYear(int year) {
          this.year = year;
     }

     public int getMonth() {
          return month;
     }

     public void setMonth(int month) {
          this.month = month;
     }

     public String getLabel() {
          return label;
     }

     public void setLabel(String label) {
          this.label = label;
     }

     public List<CalendarDay> getDays() {
          return days;
     }

     public void setDays(List<CalendarDay> days) {
          this.days = days;
     }

     public static class CalendarDay {
          private LocalDate date;
          private int day;
          private boolean inMonth;
          private boolean available;
          private BigDecimal priceAdult;
          private String priceLabel;
          private BigDecimal priceChild;
          private String priceChildLabel;

          public LocalDate getDate() {
               return date;
          }

          public void setDate(LocalDate date) {
               this.date = date;
          }

          public int getDay() {
               return day;
          }

          public void setDay(int day) {
               this.day = day;
          }

          public boolean isInMonth() {
               return inMonth;
          }

          public void setInMonth(boolean inMonth) {
               this.inMonth = inMonth;
          }

          public boolean isAvailable() {
               return available;
          }

          public void setAvailable(boolean available) {
               this.available = available;
          }

          public BigDecimal getPriceAdult() {
               return priceAdult;
          }

          public void setPriceAdult(BigDecimal priceAdult) {
               this.priceAdult = priceAdult;
          }

          public String getPriceLabel() {
               return priceLabel;
          }

          public void setPriceLabel(String priceLabel) {
               this.priceLabel = priceLabel;
          }

          public BigDecimal getPriceChild() {
               return priceChild;
          }

          public void setPriceChild(BigDecimal priceChild) {
               this.priceChild = priceChild;
          }

          public String getPriceChildLabel() {
               return priceChildLabel;
          }

          public void setPriceChildLabel(String priceChildLabel) {
               this.priceChildLabel = priceChildLabel;
          }
     }
}
