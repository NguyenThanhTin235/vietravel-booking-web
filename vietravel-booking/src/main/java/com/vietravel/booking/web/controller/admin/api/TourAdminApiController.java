package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.tour.TourAdminService;
import com.vietravel.booking.web.dto.tour.TourAdminDetailResponse;
import com.vietravel.booking.web.dto.tour.TourAdminListResponse;
import com.vietravel.booking.web.dto.tour.TourUpsertRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tours")
public class TourAdminApiController {

     private final TourAdminService service;

     public TourAdminApiController(TourAdminService service) {
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

     @PostMapping
     public ResponseEntity<TourAdminDetailResponse> create(@RequestBody TourUpsertRequest req) {
          return ResponseEntity.ok(service.create(req));
     }

     @PutMapping("/{id}")
     public ResponseEntity<TourAdminDetailResponse> update(@PathVariable Long id, @RequestBody TourUpsertRequest req) {
          return ResponseEntity.ok(service.update(id, req));
     }

     @PatchMapping("/{id}/toggle")
     public ResponseEntity<TourAdminDetailResponse> toggle(@PathVariable Long id) {
          return ResponseEntity.ok(service.toggle(id));
     }
}
