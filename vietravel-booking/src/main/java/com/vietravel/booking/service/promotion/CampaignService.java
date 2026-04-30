package com.vietravel.booking.service.promotion;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.promotion.*;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.domain.repository.promotion.CampaignRedemptionRepository;
import com.vietravel.booking.domain.repository.promotion.CampaignRepository;
import com.vietravel.booking.domain.repository.promotion.CampaignScopeRepository;
import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.promotion.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CampaignService {

     private final CampaignRepository campaignRepository;
     private final CampaignScopeRepository scopeRepository;
     private final CampaignRedemptionRepository redemptionRepository;
     private final TourRepository tourRepository;
     private final TourCategoryRepository tourCategoryRepository;

     public CampaignService(CampaignRepository campaignRepository,
               CampaignScopeRepository scopeRepository,
               CampaignRedemptionRepository redemptionRepository,
               TourRepository tourRepository,
               TourCategoryRepository tourCategoryRepository) {
          this.campaignRepository = campaignRepository;
          this.scopeRepository = scopeRepository;
          this.redemptionRepository = redemptionRepository;
          this.tourRepository = tourRepository;
          this.tourCategoryRepository = tourCategoryRepository;
     }

     @Transactional(readOnly = true)
     public List<CampaignResponse> listAll() {
          List<Campaign> items = campaignRepository.findAll();
          return toResponses(items, true);
     }

     @Transactional(readOnly = true)
     public CampaignResponse get(Long id) {
          Objects.requireNonNull(id, "id");
          Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));
          return toResponse(campaign, true);
     }

     @Transactional
     public CampaignResponse create(CampaignUpsertRequest req) {
          normalize(req);
          if (campaignRepository.existsBySlug(req.getSlug())) {
               throw new RuntimeException("Slug đã tồn tại");
          }
          if (campaignRepository.existsByCode(req.getCode())) {
               throw new RuntimeException("Mã giảm giá đã tồn tại");
          }

          Campaign campaign = new Campaign();
          applyToEntity(campaign, req);
          Campaign saved = campaignRepository.save(campaign);
          saveScopes(saved, req.getScopes());
          return toResponse(saved, true);
     }

     @Transactional
     public CampaignResponse update(Long id, CampaignUpsertRequest req) {
          Objects.requireNonNull(id, "id");
          normalize(req);

          Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));

          campaignRepository.findBySlug(req.getSlug()).ifPresent(exist -> {
               if (!exist.getId().equals(id)) {
                    throw new RuntimeException("Slug đã tồn tại");
               }
          });

          campaignRepository.findByCode(req.getCode()).ifPresent(exist -> {
               if (!exist.getId().equals(id)) {
                    throw new RuntimeException("Mã giảm giá đã tồn tại");
               }
          });

          applyToEntity(campaign, req);
          Campaign saved = campaignRepository.save(campaign);
          scopeRepository.deleteByCampaignId(saved.getId());
          saveScopes(saved, req.getScopes());
          return toResponse(saved, true);
     }

     @Transactional
     public void delete(Long id) {
          Objects.requireNonNull(id, "id");
          if (!campaignRepository.existsById(id)) {
               throw new RuntimeException("Không tìm thấy chiến dịch");
          }
          scopeRepository.deleteByCampaignId(id);
          campaignRepository.deleteById(id);
     }

     @Transactional(readOnly = true)
     public List<CampaignPublicListItem> listPublic() {
          LocalDateTime now = LocalDateTime.now();
          List<Campaign> campaigns = campaignRepository.findForPublic(now);
          return campaigns.stream().map(this::toPublicListItem).toList();
     }

     @Transactional
     public CampaignPublicDetailView getPublicDetail(String slug) {
          if (slug == null || slug.isBlank()) {
               throw new RuntimeException("Không tìm thấy chiến dịch");
          }
          Campaign campaign = campaignRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));
          campaign.setViewCount(safeInt(campaign.getViewCount()) + 1);
          campaignRepository.save(campaign);
          return toPublicDetail(campaign);
     }

     @Transactional(readOnly = true)
     public CampaignDiscountResult previewDiscount(String code, Tour tour, BigDecimal totalAmount, UserAccount user) {
          Campaign campaign = findByCode(code);
          validateCampaign(campaign, tour, totalAmount, user, true);
          BigDecimal discount = calculateDiscount(campaign, totalAmount);
          return new CampaignDiscountResult(campaign, discount, totalAmount);
     }

     @Transactional
     public CampaignDiscountResult applyDiscountForBooking(String code, Tour tour, BigDecimal totalAmount,
               UserAccount user) {
          Campaign campaign = campaignRepository.findForUpdateByCode(normalizeCode(code))
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
          validateCampaign(campaign, tour, totalAmount, user, true);
          BigDecimal discount = calculateDiscount(campaign, totalAmount);
          campaign.setUsedCount(safeInt(campaign.getUsedCount()) + 1);
          campaignRepository.save(campaign);
          return new CampaignDiscountResult(campaign, discount, totalAmount);
     }

     @Transactional
     public void redeemCampaign(Campaign campaign, UserAccount user, Booking booking, BigDecimal discountAmount) {
          if (campaign == null || user == null || booking == null) {
               return;
          }
          CampaignRedemption redemption = new CampaignRedemption();
          redemption.setCampaign(campaign);
          redemption.setUser(user);
          redemption.setBooking(booking);
          redemption.setDiscountAmount(discountAmount == null ? BigDecimal.ZERO : discountAmount);
          redemptionRepository.save(redemption);
     }

     private void validateCampaign(Campaign campaign, Tour tour, BigDecimal totalAmount, UserAccount user,
               boolean checkUsage) {
          if (campaign == null) {
               throw new RuntimeException("Mã giảm giá không tồn tại");
          }
          if (campaign.getStatus() != CampaignStatus.ACTIVE) {
               throw new RuntimeException("Mã giảm giá chưa khả dụng");
          }
          LocalDateTime now = LocalDateTime.now();
          if (campaign.getStartAt() != null && now.isBefore(campaign.getStartAt())) {
               throw new RuntimeException("Mã giảm giá chưa bắt đầu");
          }
          if (campaign.getEndAt() != null && now.isAfter(campaign.getEndAt())) {
               throw new RuntimeException("Mã giảm giá đã hết hạn");
          }
          BigDecimal minOrder = campaign.getMinOrder() == null ? BigDecimal.ZERO : campaign.getMinOrder();
          BigDecimal amount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
          if (amount.compareTo(minOrder) < 0) {
               throw new RuntimeException("Đơn hàng chưa đủ điều kiện tối thiểu");
          }
          if (checkUsage && campaign.getUsageLimit() != null && campaign.getUsageLimit() > 0) {
               int used = safeInt(campaign.getUsedCount());
               if (used >= campaign.getUsageLimit()) {
                    throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
               }
          }
          if (user == null || user.getId() == null) {
               throw new RuntimeException("Vui lòng đăng nhập để dùng mã giảm giá");
          }
          if (campaign.getPerUserLimit() != null && campaign.getPerUserLimit() > 0) {
               long used = redemptionRepository.countByCampaignIdAndUserId(campaign.getId(), user.getId());
               if (used >= campaign.getPerUserLimit()) {
                    throw new RuntimeException("Bạn đã sử dụng mã này");
               }
          }
          if (!isInScope(campaign, tour)) {
               throw new RuntimeException("Mã giảm giá không áp dụng cho tour này");
          }
     }

     private boolean isInScope(Campaign campaign, Tour tour) {
          if (tour == null || campaign == null || campaign.getId() == null) {
               return false;
          }
          List<CampaignScope> scopes = scopeRepository.findByCampaignId(campaign.getId());
          if (scopes == null || scopes.isEmpty()) {
               return true;
          }
          for (CampaignScope scope : scopes) {
               if (scope.getScopeType() == CampaignScopeType.ALL) {
                    return true;
               }
          }
          Long tourId = tour.getId();
          Set<Long> categoryIds = tour.getCategories() == null
                    ? Set.of()
                    : tour.getCategories().stream()
                              .filter(Objects::nonNull)
                              .map(TourCategory::getId)
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
          for (CampaignScope scope : scopes) {
               if (scope.getScopeType() == CampaignScopeType.TOUR && scope.getRefId() != null) {
                    if (tourId != null && tourId.equals(scope.getRefId())) {
                         return true;
                    }
               }
               if (scope.getScopeType() == CampaignScopeType.CATEGORY && scope.getRefId() != null) {
                    if (categoryIds.contains(scope.getRefId())) {
                         return true;
                    }
               }
          }
          return false;
     }

     private BigDecimal calculateDiscount(Campaign campaign, BigDecimal totalAmount) {
          BigDecimal amount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
          if (amount.compareTo(BigDecimal.ZERO) <= 0) {
               return BigDecimal.ZERO;
          }
          BigDecimal discount;
          if (campaign.getDiscountType() == CampaignDiscountType.PERCENT) {
               BigDecimal rate = campaign.getDiscountValue() == null ? BigDecimal.ZERO : campaign.getDiscountValue();
               discount = amount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
          } else {
               discount = campaign.getDiscountValue() == null ? BigDecimal.ZERO : campaign.getDiscountValue();
          }
          if (campaign.getMaxDiscount() != null && discount.compareTo(campaign.getMaxDiscount()) > 0) {
               discount = campaign.getMaxDiscount();
          }
          if (discount.compareTo(amount) > 0) {
               discount = amount;
          }
          return discount;
     }

     private Campaign findByCode(String code) {
          return campaignRepository.findByCode(normalizeCode(code))
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
     }

     private void normalize(CampaignUpsertRequest req) {
          if (req == null) {
               throw new RuntimeException("Dữ liệu không hợp lệ");
          }
          if (req.getName() == null || req.getName().trim().isEmpty()) {
               throw new RuntimeException("Tên không được rỗng");
          }
          req.setName(req.getName().trim());
          if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
               req.setSlug(slugify(req.getName()));
          } else {
               req.setSlug(slugify(req.getSlug()));
          }
          if (req.getCode() == null || req.getCode().trim().isEmpty()) {
               throw new RuntimeException("Mã giảm giá không được rỗng");
          }
          req.setCode(normalizeCode(req.getCode()));
          if (req.getStartAt() == null || req.getEndAt() == null) {
               throw new RuntimeException("Thời gian không hợp lệ");
          }
          if (req.getEndAt().isBefore(req.getStartAt())) {
               throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu");
          }
          if (req.getDiscountType() == null) {
               throw new RuntimeException("Vui lòng chọn loại giảm giá");
          }
          if (req.getDiscountValue() == null || req.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
               throw new RuntimeException("Giá trị giảm giá không hợp lệ");
          }
          if (req.getDiscountType() == CampaignDiscountType.PERCENT
                    && req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
               throw new RuntimeException("Giá trị phần trăm không hợp lệ");
          }
          if (req.getMinOrder() == null || req.getMinOrder().compareTo(BigDecimal.ZERO) < 0) {
               req.setMinOrder(BigDecimal.ZERO);
          }
          if (req.getMaxDiscount() != null && req.getMaxDiscount().compareTo(BigDecimal.ZERO) <= 0) {
               req.setMaxDiscount(null);
          }
          if (req.getUsageLimit() == null || req.getUsageLimit() < 0) {
               req.setUsageLimit(0);
          }
          if (req.getPerUserLimit() == null || req.getPerUserLimit() < 1) {
               req.setPerUserLimit(1);
          }
          if (req.getStatus() == null) {
               req.setStatus(CampaignStatus.SCHEDULE);
          }

          if (req.getScopes() == null || req.getScopes().isEmpty()) {
               CampaignScopeRequest scope = new CampaignScopeRequest();
               scope.setScopeType(CampaignScopeType.ALL);
               req.setScopes(List.of(scope));
               return;
          }

          boolean hasAll = req.getScopes().stream()
                    .anyMatch(s -> s != null && s.getScopeType() == CampaignScopeType.ALL);
          if (hasAll) {
               CampaignScopeRequest scope = new CampaignScopeRequest();
               scope.setScopeType(CampaignScopeType.ALL);
               req.setScopes(List.of(scope));
               return;
          }

          for (CampaignScopeRequest scope : req.getScopes()) {
               if (scope == null || scope.getScopeType() == null) {
                    throw new RuntimeException("Phạm vi áp dụng không hợp lệ");
               }
               if (scope.getScopeType() != CampaignScopeType.ALL && scope.getRefId() == null) {
                    throw new RuntimeException("Thiếu đối tượng áp dụng");
               }
               if (scope.getScopeType() == CampaignScopeType.TOUR) {
                    if (!tourRepository.existsById(scope.getRefId())) {
                         throw new RuntimeException("Tour không tồn tại");
                    }
               }
               if (scope.getScopeType() == CampaignScopeType.CATEGORY) {
                    if (!tourCategoryRepository.existsById(scope.getRefId())) {
                         throw new RuntimeException("Danh mục tour không tồn tại");
                    }
               }
          }
     }

     private void applyToEntity(Campaign campaign, CampaignUpsertRequest req) {
          campaign.setName(req.getName());
          campaign.setSlug(req.getSlug());
          campaign.setDescription(req.getDescription());
          campaign.setBannerUrl(req.getBannerUrl());
          campaign.setStartAt(req.getStartAt());
          campaign.setEndAt(req.getEndAt());
          campaign.setStatus(req.getStatus());
          campaign.setDiscountType(req.getDiscountType());
          campaign.setDiscountValue(req.getDiscountValue());
          campaign.setCode(req.getCode());
          campaign.setMinOrder(req.getMinOrder());
          campaign.setMaxDiscount(req.getMaxDiscount());
          campaign.setUsageLimit(req.getUsageLimit());
          campaign.setPerUserLimit(req.getPerUserLimit());
     }

     private void saveScopes(Campaign campaign, List<CampaignScopeRequest> scopes) {
          if (campaign == null || scopes == null || scopes.isEmpty()) {
               return;
          }
          for (CampaignScopeRequest s : scopes) {
               if (s == null || s.getScopeType() == null) {
                    continue;
               }
               CampaignScope scope = new CampaignScope();
               scope.setCampaign(campaign);
               scope.setScopeType(s.getScopeType());
               scope.setRefId(s.getRefId());
               scopeRepository.save(scope);
          }
     }

     private List<CampaignResponse> toResponses(List<Campaign> campaigns, boolean includeScopes) {
          if (campaigns == null || campaigns.isEmpty()) {
               return List.of();
          }
          return campaigns.stream().map(c -> toResponse(c, includeScopes)).toList();
     }

     private CampaignResponse toResponse(Campaign campaign, boolean includeScopes) {
          CampaignResponse res = new CampaignResponse();
          res.setId(campaign.getId());
          res.setName(campaign.getName());
          res.setSlug(campaign.getSlug());
          res.setDescription(campaign.getDescription());
          res.setBannerUrl(campaign.getBannerUrl());
          res.setViewCount(campaign.getViewCount());
          res.setStartAt(campaign.getStartAt());
          res.setEndAt(campaign.getEndAt());
          res.setStatus(campaign.getStatus());
          res.setDiscountType(campaign.getDiscountType());
          res.setDiscountValue(campaign.getDiscountValue());
          res.setCode(campaign.getCode());
          res.setMinOrder(campaign.getMinOrder());
          res.setMaxDiscount(campaign.getMaxDiscount());
          res.setUsageLimit(campaign.getUsageLimit());
          res.setUsedCount(campaign.getUsedCount());
          res.setPerUserLimit(campaign.getPerUserLimit());
          res.setCreatedAt(campaign.getCreatedAt());
          res.setUpdatedAt(campaign.getUpdatedAt());
          if (includeScopes) {
               res.setScopes(toScopeResponses(scopeRepository.findByCampaignId(campaign.getId())));
          }
          return res;
     }

     private List<CampaignScopeResponse> toScopeResponses(List<CampaignScope> scopes) {
          if (scopes == null || scopes.isEmpty()) {
               return List.of();
          }
          Set<Long> categoryIds = scopes.stream()
                    .filter(s -> s.getScopeType() == CampaignScopeType.CATEGORY && s.getRefId() != null)
                    .map(CampaignScope::getRefId)
                    .collect(Collectors.toSet());
          Set<Long> tourIds = scopes.stream()
                    .filter(s -> s.getScopeType() == CampaignScopeType.TOUR && s.getRefId() != null)
                    .map(CampaignScope::getRefId)
                    .collect(Collectors.toSet());

          Map<Long, String> categoryNames = new HashMap<>();
          if (!categoryIds.isEmpty()) {
               for (TourCategory c : tourCategoryRepository.findAllById(categoryIds)) {
                    categoryNames.put(c.getId(), c.getName());
               }
          }

          Map<Long, String> tourNames = new HashMap<>();
          if (!tourIds.isEmpty()) {
               for (Tour t : tourRepository.findAllById(tourIds)) {
                    tourNames.put(t.getId(), t.getTitle());
               }
          }

          List<CampaignScopeResponse> results = new ArrayList<>();
          for (CampaignScope scope : scopes) {
               CampaignScopeResponse r = new CampaignScopeResponse();
               r.setScopeType(scope.getScopeType());
               r.setRefId(scope.getRefId());
               if (scope.getScopeType() == CampaignScopeType.CATEGORY) {
                    r.setRefName(categoryNames.get(scope.getRefId()));
               } else if (scope.getScopeType() == CampaignScopeType.TOUR) {
                    r.setRefName(tourNames.get(scope.getRefId()));
               } else {
                    r.setRefName("Toàn bộ");
               }
               results.add(r);
          }
          return results;
     }

     private CampaignPublicListItem toPublicListItem(Campaign c) {
          CampaignPublicListItem r = new CampaignPublicListItem();
          r.setId(c.getId());
          r.setName(c.getName());
          r.setSlug(c.getSlug());
          r.setDescription(c.getDescription());
          r.setBannerUrl(c.getBannerUrl());
          r.setDiscountType(c.getDiscountType());
          r.setDiscountValue(c.getDiscountValue());
          r.setCode(c.getCode());
          r.setStartAt(c.getStartAt());
          r.setEndAt(c.getEndAt());
          r.setViewCount(c.getViewCount());
          return r;
     }

     private CampaignPublicDetailView toPublicDetail(Campaign c) {
          CampaignPublicDetailView r = new CampaignPublicDetailView();
          r.setId(c.getId());
          r.setName(c.getName());
          r.setSlug(c.getSlug());
          r.setDescription(c.getDescription());
          r.setBannerUrl(c.getBannerUrl());
          r.setDiscountType(c.getDiscountType());
          r.setDiscountValue(c.getDiscountValue());
          r.setCode(c.getCode());
          r.setMinOrder(c.getMinOrder());
          r.setMaxDiscount(c.getMaxDiscount());
          r.setUsageLimit(c.getUsageLimit());
          r.setUsedCount(c.getUsedCount());
          r.setStartAt(c.getStartAt());
          r.setEndAt(c.getEndAt());
          r.setViewCount(c.getViewCount());
          return r;
     }

     private String normalizeCode(String code) {
          String v = code == null ? "" : code.trim().toUpperCase();
          v = v.replaceAll("\\s+", "");
          return v;
     }

     private String slugify(String s) {
          String v = s == null ? "" : s.trim().toLowerCase();
          v = v.replace('đ', 'd');
          v = v.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
          v = v.replaceAll("[èéẹẻẽêềếệểễ]", "e");
          v = v.replaceAll("[ìíịỉĩ]", "i");
          v = v.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
          v = v.replaceAll("[ùúụủũưừứựửữ]", "u");
          v = v.replaceAll("[ỳýỵỷỹ]", "y");
          v = v.replaceAll("[^a-z0-9]+", "-");
          v = v.replaceAll("(^-+)|(-+$)", "");
          v = v.replaceAll("-+", "-");
          if (v.isEmpty()) {
               throw new RuntimeException("Slug không hợp lệ");
          }
          return v;
     }

     private int safeInt(Integer v) {
          return v == null ? 0 : v;
     }

     public static class CampaignDiscountResult {
          private final Campaign campaign;
          private final BigDecimal discountAmount;
          private final BigDecimal originalAmount;

          public CampaignDiscountResult(Campaign campaign, BigDecimal discountAmount, BigDecimal originalAmount) {
               this.campaign = campaign;
               this.discountAmount = discountAmount;
               this.originalAmount = originalAmount;
          }

          public Campaign getCampaign() {
               return campaign;
          }

          public BigDecimal getDiscountAmount() {
               return discountAmount;
          }

          public BigDecimal getOriginalAmount() {
               return originalAmount;
          }
     }
}
