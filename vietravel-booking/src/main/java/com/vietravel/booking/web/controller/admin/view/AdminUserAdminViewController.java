package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminUserAdminViewController{

    @GetMapping("/admin/users")
    public String index(Model model){
        model.addAttribute("pageTitle","Người dùng");
        model.addAttribute("activeMenu","tour");
        model.addAttribute("activeSubMenu","users");
        return "admin/users/page";
    }

    @GetMapping("/admin/users/create-staff")
    public String createStaff(Model model){
        model.addAttribute("pageTitle","Thêm nhân viên");
        model.addAttribute("activeMenu","tour");
        model.addAttribute("activeSubMenu","users");
        model.addAttribute("userId",null);
        model.addAttribute("mode","createStaff");
        return "admin/users/form-page";
    }

    @GetMapping("/admin/users/edit")
    public String edit(Model model,Long id){
        model.addAttribute("pageTitle","Cập nhật người dùng");
        model.addAttribute("activeMenu","tour");
        model.addAttribute("activeSubMenu","users");
        model.addAttribute("userId",id);
        model.addAttribute("mode","edit");
        return "admin/users/form-page";
    }

    @GetMapping("/admin/users/view")
    public String viewUser(@RequestParam Long id, Model model){
        model.addAttribute("pageTitle","Chi tiết người dùng");
        model.addAttribute("activeMenu","users");
        model.addAttribute("userId",id);
        model.addAttribute("content","admin/users/view :: content");
        return "admin/users/view-page";
    }

}
