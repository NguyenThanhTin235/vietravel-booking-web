package com.vietravel.booking.domain.proxy;

import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.web.dto.booking.BookingCreateRequest.PassengerRequest;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class DepartureAvailabilityProxy implements DepartureProxy {
     private String errorMessage = null;

     @Override
     public boolean isAvailable(Departure departure, int totalRequest) {
          if (departure == null || departure.getCapacity() == null) {
               errorMessage = "Số lượng còn lại không khả dụng. Vui lòng chọn ngày khởi hành khác.(PROXY)";
               return false;
          }
          Integer available = departure.getAvailable() != null ? departure.getAvailable() : 0;
          if (available < totalRequest) {
               errorMessage = "Số lượng còn lại không khả dụng. Vui lòng chọn ngày khởi hành khác.(PROXY)";
               return false;
          }
          return true;
     }

     // Validate độ tuổi phù hợp cho danh sách hành khách
     public boolean validateAges(List<PassengerRequest> passengers, LocalDate departureDate) {
          if (passengers == null || departureDate == null)
               return true;
          for (PassengerRequest p : passengers) {
               if (p == null || p.getDob() == null || p.getType() == null)
                    continue;
               int age = Period.between(p.getDob(), departureDate).getYears();
               if (p.getType().name().equalsIgnoreCase("ADULT")) {
                    if (age < 12) {
                         errorMessage = "Người lớn phải đủ 12 tuổi trở lên.(PROXY)";
                         return false;
                    }
               } else if (p.getType().name().equalsIgnoreCase("CHILD")) {
                    if (age >= 12) {
                         errorMessage = "Trẻ em phải dưới 12 tuổi.(PROXY)";
                         return false;
                    }
               }
          }
          return true;
     }

     @Override
     public String getUnavailableMessage() {
          return errorMessage != null ? errorMessage
                    : "Số lượng còn lại không khả dụng. Vui lòng chọn ngày khởi hành khác. (PROXY)";
     }
}
