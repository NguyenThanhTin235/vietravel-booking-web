package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.promotion.CampaignService;
import com.vietravel.booking.web.dto.promotion.CampaignPublicDetailView;
import com.vietravel.booking.web.dto.promotion.CampaignPublicListItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

     private final CampaignService campaignService;

     public PromotionController(CampaignService campaignService) {
          this.campaignService = campaignService;
     }

     @GetMapping
     public String list(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
               Model model) {
          List<CampaignPublicListItem> all = campaignService.listPublic();
          int pageSize = 6;
          int totalItems = all.size();
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int safePage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
          int start = (safePage - 1) * pageSize;
          int end = Math.min(start + pageSize, totalItems);
          List<CampaignPublicListItem> pageItems = start < end ? all.subList(start, end) : List.of();

          model.addAttribute("pageTitle", "Khuyến mãi");
          model.addAttribute("activeNav", "promotion");
          model.addAttribute("campaigns", pageItems);
          model.addAttribute("currentPage", safePage);
          model.addAttribute("totalPages", totalPages);
          return "public/promotions/list";
     }

     @GetMapping("/{slug}")
     public String detail(@PathVariable("slug") String slug, Model model) {
          CampaignPublicDetailView detail = campaignService.getPublicDetail(slug);
          model.addAttribute("pageTitle", detail.getName());
          model.addAttribute("activeNav", "promotion");
          model.addAttribute("campaign", detail);
          return "public/promotions/detail";
     }
}
