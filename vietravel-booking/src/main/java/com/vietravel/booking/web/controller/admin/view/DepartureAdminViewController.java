package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/departures")
public class DepartureAdminViewController {

     @GetMapping
     public String index(Model model) {
          model.addAttribute("pageTitle", "Quản lý ngày khởi hành");
          model.addAttribute("activeMenu", "departures");
          model.addAttribute("activeSubMenu", "");
          return "admin/departures/page";
     }
}
