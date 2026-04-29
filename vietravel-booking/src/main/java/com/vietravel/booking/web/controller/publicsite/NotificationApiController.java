package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.support.NotificationService;
import com.vietravel.booking.web.dto.support.NotificationResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

     private final NotificationService notificationService;

     public NotificationApiController(NotificationService notificationService) {
          this.notificationService = notificationService;
     }

     @GetMapping
     public List<NotificationResponse> list(@RequestParam(defaultValue = "20") int limit) {
          return notificationService.listCurrent(limit);
     }

     @GetMapping("/unread-count")
     public Map<String, Long> unreadCount() {
          return Map.of("count", notificationService.unreadCount());
     }

     @GetMapping("/{id}")
     public NotificationResponse detail(@PathVariable Long id) {
          return notificationService.getDetail(id);
     }

     @PostMapping("/{id}/read")
     public void markRead(@PathVariable Long id) {
          notificationService.markRead(id);
     }

     @DeleteMapping("/{id}")
     public void delete(@PathVariable Long id) {
          notificationService.delete(id);
     }
}
