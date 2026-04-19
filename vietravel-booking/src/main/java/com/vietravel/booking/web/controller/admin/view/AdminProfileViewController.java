package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProfileViewController {

     @GetMapping("/admin/profile")
     public String profile(Model model) {
          model.addAttribute("pageTitle", "Hồ sơ quản trị");
          model.addAttribute("activeMenu", "");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("content", "admin/profile/index :: content");
          return "admin/profile/page";
     }
}
