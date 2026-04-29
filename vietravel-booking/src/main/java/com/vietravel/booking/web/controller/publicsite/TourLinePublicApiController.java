package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.tour.TourLineApiService;
import com.vietravel.booking.web.dto.tour.TourLineResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tour-lines")
public class TourLinePublicApiController {

     private final TourLineApiService service;

     public TourLinePublicApiController(TourLineApiService service) {
          this.service = service;
     }

     @GetMapping
     public List<TourLineResponse> list(@RequestParam(value = "active", required = false) Boolean active) {
          return service.list(active);
     }
}
