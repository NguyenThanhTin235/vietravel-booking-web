package com.vietravel.booking.web.controller.admin.view;

import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DestinationAdminViewController{

    private final TourCategoryRepository tourCategoryRepository;

    public DestinationAdminViewController(TourCategoryRepository tourCategoryRepository){
        this.tourCategoryRepository=tourCategoryRepository;
    }

    @GetMapping("/admin/destinations")
    public String index(Model model){
        model.addAttribute("pageTitle","Quản lý điểm đến");
        model.addAttribute("activeMenu","destinations");
        model.addAttribute("activeSubMenu","");
        model.addAttribute("categories",tourCategoryRepository.findAll());
        return "admin/destinations/page";
    }

    @GetMapping("/admin/destinations/create")
    public String create(Model model){
        model.addAttribute("pageTitle","Thêm điểm đến");
        model.addAttribute("activeMenu","destinations");
        model.addAttribute("activeSubMenu","");
        model.addAttribute("destinationId",null);
        model.addAttribute("categories",tourCategoryRepository.findAll());
        return "admin/destinations/form-page";
    }

    @GetMapping("/admin/destinations/edit")
    public String edit(@RequestParam("id") Long id,Model model){
        model.addAttribute("pageTitle","Cập nhật điểm đến");
        model.addAttribute("activeMenu","destinations");
        model.addAttribute("activeSubMenu","");
        model.addAttribute("destinationId",id);
        model.addAttribute("categories",tourCategoryRepository.findAll());
        return "admin/destinations/form-page";
    }
}
