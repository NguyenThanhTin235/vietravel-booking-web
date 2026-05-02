package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.content.NewsService;
import com.vietravel.booking.web.dto.content.NewsPublicDetailView;
import com.vietravel.booking.web.dto.content.NewsPublicListItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/news")
public class NewsController {

     private final NewsService newsService;

     public NewsController(NewsService newsService) {
          this.newsService = newsService;
     }

     @GetMapping
     public String list(@RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
          List<NewsPublicListItem> all = newsService.listPublic();
          int pageSize = 6;
          int totalItems = all.size();
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int safePage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
          int start = (safePage - 1) * pageSize;
          int end = Math.min(start + pageSize, totalItems);
          List<NewsPublicListItem> pageItems = start < end ? all.subList(start, end) : List.of();

          model.addAttribute("pageTitle", "Tin tức");
          model.addAttribute("activeNav", "news");
          model.addAttribute("newsItems", pageItems);
          model.addAttribute("currentPage", safePage);
          model.addAttribute("totalPages", totalPages);
          return "public/news/list";
     }

     @GetMapping("/{slug}")
     public String detail(@PathVariable("slug") String slug, Model model) {
          NewsPublicDetailView detail = newsService.getPublicDetail(slug);
          model.addAttribute("pageTitle", detail.getTitle());
          model.addAttribute("activeNav", "news");
          model.addAttribute("news", detail);
          return "public/news/detail";
     }
}
