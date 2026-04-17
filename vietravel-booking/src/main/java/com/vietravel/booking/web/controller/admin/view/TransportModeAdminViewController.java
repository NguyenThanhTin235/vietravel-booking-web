package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/transports")
public class TransportModeAdminViewController{

    @GetMapping
    public String index(Model model){
        model.addAttribute("pageTitle","Phương tiện");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","transports");
        return "admin/transports/page";
    }

    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pageTitle","Thêm phương tiện");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","transports");
        return "admin/transports/form-page";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam Long id,Model model){
        model.addAttribute("pageTitle","Sửa phương tiện");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","transports");
        model.addAttribute("transportModeId",id);
        return "admin/transports/form-page";
    }
}
