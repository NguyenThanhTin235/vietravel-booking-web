package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.promotion.CampaignService;
import com.vietravel.booking.web.dto.promotion.CampaignResponse;
import com.vietravel.booking.web.dto.promotion.CampaignUpsertRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/campaigns")
public class CampaignAdminApiController {

     private final CampaignService campaignService;

     public CampaignAdminApiController(CampaignService campaignService) {
          this.campaignService = campaignService;
     }

     @GetMapping
     public List<CampaignResponse> list() {
          return campaignService.listAll();
     }

     @GetMapping("/{id}")
     public CampaignResponse get(@PathVariable Long id) {
          return campaignService.get(id);
     }

     @PostMapping
     public CampaignResponse create(@RequestBody CampaignUpsertRequest req) {
          return campaignService.create(req);
     }

     @PutMapping("/{id}")
     public CampaignResponse update(@PathVariable Long id, @RequestBody CampaignUpsertRequest req) {
          return campaignService.update(id, req);
     }

     @DeleteMapping("/{id}")
     public void delete(@PathVariable Long id) {
          campaignService.delete(id);
     }
}
