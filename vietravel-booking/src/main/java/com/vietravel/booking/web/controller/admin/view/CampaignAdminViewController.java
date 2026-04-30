package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/campaigns")
public class CampaignAdminViewController {

     @GetMapping
     public String index(Model model) {
          model.addAttribute("pageTitle", "Chiến dịch");
          model.addAttribute("activeMenu", "campaigns");
          model.addAttribute("activeSubMenu", "");
          return "admin/campaigns/page";
     }

     @GetMapping("/create")
     public String create(Model model) {
          model.addAttribute("pageTitle", "Thêm chiến dịch");
          model.addAttribute("activeMenu", "campaigns");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("campaignId", null);
          return "admin/campaigns/form-page";
     }

     @GetMapping("/edit")
     public String edit(@RequestParam("id") Long id, Model model) {
          model.addAttribute("pageTitle", "Cập nhật chiến dịch");
          model.addAttribute("activeMenu", "campaigns");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("campaignId", id);
          return "admin/campaigns/form-page";
     }
}
