package com.vietravel.booking.web.controller.admin;

import com.vietravel.booking.service.content.NewsService;
import com.vietravel.booking.web.dto.content.NewsResponse;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

     private static final Logger logger = LoggerFactory.getLogger(AdminNewsController.class);

     private final NewsService newsService;

     public AdminNewsController(NewsService newsService) {
          this.newsService = newsService;
     }

     @GetMapping("/{id}")
     public String detail(@PathVariable("id") Long id, Model model) {
          NewsResponse news = null;
          try {
               news = newsService.get(id);
          } catch (Exception ex) {
               logger.error("Error fetching news detail for id {}: {}", id, ex.getMessage(), ex);
          }
          if (news == null) {
               return "redirect:/admin/news?notfound=1";
          }
          model.addAttribute("news", news);
          model.addAttribute("pageTitle", news.getTitle());
          model.addAttribute("activeMenu", "news");
          model.addAttribute("activeSubMenu", "news-list");
          return "admin/news/detail";
     }
}
