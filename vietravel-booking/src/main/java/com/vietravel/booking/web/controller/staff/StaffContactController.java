package com.vietravel.booking.web.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/inquiries")
public class StaffContactController {

     @GetMapping
     public String inquiries(Model model) {
          model.addAttribute("pageTitle", "Xử lý thắc mắc");
          model.addAttribute("activeMenu", "inquiries");
          model.addAttribute("activeSubMenu", "");
          return "staff/inquiries/index";
     }
}
