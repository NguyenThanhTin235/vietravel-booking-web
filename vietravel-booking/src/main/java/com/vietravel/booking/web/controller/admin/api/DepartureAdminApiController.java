package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.tour.DepartureService;
import com.vietravel.booking.web.dto.tour.DepartureAdminResponse;
import com.vietravel.booking.web.dto.tour.DepartureUpsertRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departures")
public class DepartureAdminApiController {

     private final DepartureService departureService;

     public DepartureAdminApiController(DepartureService departureService) {
          this.departureService = departureService;
     }

     @GetMapping
     public ResponseEntity<List<DepartureAdminResponse>> list(
               @RequestParam(value = "tourId", required = false) Long tourId,
               @RequestParam("year") int year,
               @RequestParam("month") int month) {
          return ResponseEntity.ok(departureService.listCalendar(tourId, year, month));
     }

     @PostMapping
     public ResponseEntity<DepartureAdminResponse> create(@RequestBody DepartureUpsertRequest req) {
          return ResponseEntity.ok(departureService.create(req));
     }

     @PutMapping("/{id}")
     public ResponseEntity<DepartureAdminResponse> update(
               @PathVariable Long id,
               @RequestBody DepartureUpsertRequest req) {
          return ResponseEntity.ok(departureService.update(id, req));
     }

     @DeleteMapping("/{id}")
     public ResponseEntity<Void> delete(@PathVariable Long id) {
          departureService.delete(id);
          return ResponseEntity.noContent().build();
     }
}
