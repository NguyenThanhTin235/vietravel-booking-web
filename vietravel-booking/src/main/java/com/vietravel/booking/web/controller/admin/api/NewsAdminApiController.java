package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.content.NewsService;
import com.vietravel.booking.web.dto.content.NewsResponse;
import com.vietravel.booking.web.dto.content.NewsUpsertRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/news")
public class NewsAdminApiController {

     private final NewsService newsService;

     public NewsAdminApiController(NewsService newsService) {
          this.newsService = newsService;
     }

     @GetMapping
     public List<NewsResponse> list() {
          return newsService.listAll();
     }

     @GetMapping("/{id}")
     public NewsResponse get(@PathVariable Long id) {
          return newsService.get(id);
     }

     @PostMapping
     public NewsResponse create(@RequestBody NewsUpsertRequest req) {
          return newsService.create(req);
     }

     @PutMapping("/{id}")
     public NewsResponse update(@PathVariable Long id, @RequestBody NewsUpsertRequest req) {
          return newsService.update(id, req);
     }

     @DeleteMapping("/{id}")
     public void delete(@PathVariable Long id) {
          newsService.delete(id);
     }
}
