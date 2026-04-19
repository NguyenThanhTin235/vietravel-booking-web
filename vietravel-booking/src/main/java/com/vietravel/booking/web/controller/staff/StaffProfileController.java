package com.vietravel.booking.web.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/profile")
public class StaffProfileController {

     @GetMapping
     public String profile(Model model) {
          model.addAttribute("pageTitle", "Hồ sơ nhân viên");
          model.addAttribute("activeMenu", "");
          model.addAttribute("activeSubMenu", "");
          return "staff/profile/index";
     }
}
