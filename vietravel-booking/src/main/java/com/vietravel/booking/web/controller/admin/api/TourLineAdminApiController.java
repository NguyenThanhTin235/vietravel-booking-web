package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.tour.TourLineApiService;
import com.vietravel.booking.web.dto.tour.TourLineResponse;
import com.vietravel.booking.web.dto.tour.TourLineUpsertRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tour-lines")
public class TourLineAdminApiController{

    private final TourLineApiService service;

    public TourLineAdminApiController(TourLineApiService service){
        this.service=service;
    }

    @GetMapping
    public ResponseEntity<List<TourLineResponse>> list(@RequestParam(required=false) Boolean active){
        return ResponseEntity.ok(service.list(active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourLineResponse> get(@PathVariable Long id){
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<TourLineResponse> create(@RequestBody TourLineUpsertRequest req){
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourLineResponse> update(@PathVariable Long id,@RequestBody TourLineUpsertRequest req){
        return ResponseEntity.ok(service.update(id,req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resolve")
    public ResponseEntity<TourLineResponse> resolve(@RequestParam BigDecimal price){
        return ResponseEntity.ok(service.resolveByPrice(price));
    }
}
