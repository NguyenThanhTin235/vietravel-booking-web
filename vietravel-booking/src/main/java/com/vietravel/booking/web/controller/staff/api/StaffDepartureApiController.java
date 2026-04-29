package com.vietravel.booking.web.controller.staff.api;

import com.vietravel.booking.service.tour.DepartureService;
import com.vietravel.booking.web.dto.tour.DepartureAdminResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/departures")
public class StaffDepartureApiController {

     private final DepartureService departureService;

     public StaffDepartureApiController(DepartureService departureService) {
          this.departureService = departureService;
     }

     @GetMapping
     public ResponseEntity<List<DepartureAdminResponse>> list(
               @RequestParam(value = "tourId", required = false) Long tourId,
               @RequestParam("year") int year,
               @RequestParam("month") int month) {
          return ResponseEntity.ok(departureService.listCalendar(tourId, year, month));
     }
}
