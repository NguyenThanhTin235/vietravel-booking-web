package com.vietravel.booking.web.controller.publicsite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfilePageController {

     @GetMapping("/profile")
     public String profile(Model model) {
          model.addAttribute("pageTitle", "Hồ sơ cá nhân");
          model.addAttribute("activeNav", "");
          return "profile/index";
     }
}
