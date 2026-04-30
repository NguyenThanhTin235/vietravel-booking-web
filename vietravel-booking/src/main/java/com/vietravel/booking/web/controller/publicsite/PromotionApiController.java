package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.service.booking.BookingService;
import com.vietravel.booking.service.promotion.CampaignService;
import com.vietravel.booking.web.dto.promotion.CampaignApplyRequest;
import com.vietravel.booking.web.dto.promotion.CampaignApplyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/public/campaigns")
public class PromotionApiController {

     private final CampaignService campaignService;
     private final BookingService bookingService;

     public PromotionApiController(CampaignService campaignService, BookingService bookingService) {
          this.campaignService = campaignService;
          this.bookingService = bookingService;
     }

     @PostMapping("/apply")
     public ResponseEntity<CampaignApplyResponse> apply(@RequestBody CampaignApplyRequest req) {
          CampaignApplyResponse res = new CampaignApplyResponse();
          try {
               if (req == null || req.getCode() == null || req.getCode().isBlank()) {
                    throw new RuntimeException("Vui lòng nhập mã giảm giá");
               }
               if (req.getTourSlug() == null || req.getTourSlug().isBlank()) {
                    throw new RuntimeException("Thiếu thông tin tour");
               }
               BigDecimal amount = req.getTotalAmount() == null ? BigDecimal.ZERO : req.getTotalAmount();
               Tour tour = bookingService.loadTourBySlug(req.getTourSlug());
               var result = campaignService.previewDiscount(req.getCode(), tour, amount,
                         bookingService.getCurrentUserForPromo());
               BigDecimal discount = result.getDiscountAmount();
               res.setValid(true);
               res.setDiscountAmount(discount);
               res.setTotalAmount(amount.subtract(discount));
               res.setCampaignName(result.getCampaign().getName());
               res.setMessage("Áp dụng thành công");
               return ResponseEntity.ok(res);
          } catch (RuntimeException ex) {
               res.setValid(false);
               res.setMessage(ex.getMessage());
               return ResponseEntity.ok(res);
          }
     }
}
