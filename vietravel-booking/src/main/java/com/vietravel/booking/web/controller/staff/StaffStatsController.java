package com.vietravel.booking.web.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/stats")
public class StaffStatsController {

     @GetMapping
     public String stats(Model model) {
          model.addAttribute("pageTitle", "Thống kê");
          model.addAttribute("activeMenu", "stats");
          model.addAttribute("activeSubMenu", "");
          return "staff/stats/index";
     }
}
