package com.vietravel.booking.web.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

     @GetMapping
     public String dashboard(Model model) {
          model.addAttribute("pageTitle", "Bảng điều khiển");
          model.addAttribute("activeMenu", "dashboard");
          model.addAttribute("activeSubMenu", "");
          return "staff/index";
     }
}
