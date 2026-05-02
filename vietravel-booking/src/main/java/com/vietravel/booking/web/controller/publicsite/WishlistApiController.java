package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.tour.WishlistService;
import com.vietravel.booking.web.dto.tour.WishlistItemResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

     private final WishlistService wishlistService;

     public WishlistApiController(WishlistService wishlistService) {
          this.wishlistService = wishlistService;
     }

     @GetMapping
     public List<WishlistItemResponse> list(@RequestParam(defaultValue = "20") int limit) {
          return wishlistService.list(limit);
     }

     @GetMapping("/count")
     public Map<String, Long> count() {
          return Map.of("count", wishlistService.count());
     }

     @GetMapping("/ids")
     public List<Long> ids() {
          return wishlistService.listTourIds();
     }

     @PostMapping("/{tourId}/toggle")
     public Map<String, Boolean> toggle(@PathVariable Long tourId) {
          boolean favorited = wishlistService.toggle(tourId);
          return Map.of("favorited", favorited);
     }

     @DeleteMapping("/{tourId}")
     public void remove(@PathVariable Long tourId) {
          wishlistService.remove(tourId);
     }
}
