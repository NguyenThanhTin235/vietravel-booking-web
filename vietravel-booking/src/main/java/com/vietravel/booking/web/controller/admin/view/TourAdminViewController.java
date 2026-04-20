package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/tours")
public class TourAdminViewController {

     @GetMapping
     public String index(Model model) {
          model.addAttribute("pageTitle", "Thông tin tour");
          model.addAttribute("activeMenu", "tours");
          model.addAttribute("activeSubMenu", "tour-info");
          return "admin/tours/page";
     }

     @GetMapping("/create")
     public String create(Model model) {
          model.addAttribute("pageTitle", "Thêm tour");
          model.addAttribute("activeMenu", "tours");
          model.addAttribute("activeSubMenu", "tour-info");
          model.addAttribute("tourId", null);
          return "admin/tours/form-page";
     }

     @GetMapping("/edit")
     public String edit(@RequestParam("id") Long id, Model model) {
          model.addAttribute("pageTitle", "Cập nhật tour");
          model.addAttribute("activeMenu", "tours");
          model.addAttribute("activeSubMenu", "tour-info");
          model.addAttribute("tourId", id);
          return "admin/tours/form-page";
     }
}
