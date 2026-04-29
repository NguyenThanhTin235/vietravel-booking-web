package com.vietravel.booking.web.controller.staff.api;

import com.vietravel.booking.service.tour.TourAdminService;
import com.vietravel.booking.web.dto.tour.TourAdminDetailResponse;
import com.vietravel.booking.web.dto.tour.TourAdminListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/tours")
public class StaffTourApiController {

     private final TourAdminService service;

     public StaffTourApiController(TourAdminService service) {
          this.service = service;
     }

     @GetMapping
     public ResponseEntity<List<TourAdminListResponse>> list(
               @RequestParam(value = "active", required = false) Boolean active,
               @RequestParam(value = "categoryId", required = false) Long categoryId,
               @RequestParam(value = "q", required = false) String q) {
          return ResponseEntity.ok(service.list(active, categoryId, q));
     }

     @GetMapping("/{id}")
     public ResponseEntity<TourAdminDetailResponse> get(@PathVariable Long id) {
          return ResponseEntity.ok(service.get(id));
     }
}
