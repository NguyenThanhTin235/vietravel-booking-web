package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tour-categories")
public class TourCategoryAdminViewController{

    @GetMapping
    public String index(Model model){
        model.addAttribute("pageTitle","Danh mục tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-categories");
        return "admin/tour-categories/page";
    }

    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pageTitle","Thêm danh mục tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-categories");
        model.addAttribute("tourCategoryId",null);
        return "admin/tour-categories/form-page";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("id") Long id,Model model){
        model.addAttribute("pageTitle","Cập nhật danh mục tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-categories");
        model.addAttribute("tourCategoryId",id);
        return "admin/tour-categories/form-page";
    }
}
