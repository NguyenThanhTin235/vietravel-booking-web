package com.vietravel.booking.web.controller.publicsite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController{

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("pageTitle","Vietravel Booking");
        model.addAttribute("activeNav","home");
        return "home/index";
    }

    @ResponseBody
    @GetMapping("/health")
    public String health(){
        return "✅ Vietravel Booking is running!";
    }

    @GetMapping("/admin")
    public String homeAdmin(Model model){
        model.addAttribute("pageTitle","Bảng điều khiển");
        model.addAttribute("activeMenu","dashboard");
        model.addAttribute("activeSubMenu","");
        return "admin/index";
    }

    @GetMapping("/staff")
    public String homeStaff(Model model){
        model.addAttribute("pageTitle","Nhân viên");
        model.addAttribute("activeNav","staff");
        model.addAttribute("content","staff/index :: content");
        return "layout/base";
    }
}
