package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/news")
public class NewsAdminViewController {

     @GetMapping
     public String index(Model model) {
          model.addAttribute("pageTitle", "Tin tức");
          model.addAttribute("activeMenu", "news");
          model.addAttribute("activeSubMenu", "");
          return "admin/news/page";
     }

     @GetMapping("/create")
     public String create(Model model) {
          model.addAttribute("pageTitle", "Thêm tin tức");
          model.addAttribute("activeMenu", "news");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("newsId", null);
          return "admin/news/form-page";
     }

     @GetMapping("/edit")
     public String edit(@RequestParam("id") Long id, Model model) {
          model.addAttribute("pageTitle", "Cập nhật tin tức");
          model.addAttribute("activeMenu", "news");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("newsId", id);
          return "admin/news/form-page";
     }
}
