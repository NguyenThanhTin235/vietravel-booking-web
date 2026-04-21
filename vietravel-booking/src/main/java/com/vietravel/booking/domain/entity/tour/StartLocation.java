package com.vietravel.booking.domain.entity.tour;

public enum StartLocation {
     HN("Hà Nội"),
     DN("Đà Nẵng"),
     HCM("Hồ Chí Minh");

     private final String label;

     StartLocation(String label) {
          this.label = label;
     }

     public String getLabel() {
          return label;
     }
}
