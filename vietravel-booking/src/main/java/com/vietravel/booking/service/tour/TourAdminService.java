package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.*;
import com.vietravel.booking.domain.repository.tour.*;
import com.vietravel.booking.web.dto.tour.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.*;

@Service
public class TourAdminService {

     private final TourRepository tourRepository;
     private final TourCategoryRepository tourCategoryRepository;
     private final DestinationRepository destinationRepository;
     private final TourLineRepository tourLineRepository;
     private final TransportModeRepository transportModeRepository;
     private final ItineraryDayRepository itineraryDayRepository;

     public TourAdminService(
               TourRepository tourRepository,
               TourCategoryRepository tourCategoryRepository,
               DestinationRepository destinationRepository,
               TourLineRepository tourLineRepository,
               TransportModeRepository transportModeRepository,
               ItineraryDayRepository itineraryDayRepository) {
          this.tourRepository = tourRepository;
          this.tourCategoryRepository = tourCategoryRepository;
          this.destinationRepository = destinationRepository;
          this.tourLineRepository = tourLineRepository;
          this.transportModeRepository = transportModeRepository;
          this.itineraryDayRepository = itineraryDayRepository;
     }

     @Transactional(readOnly = true)
     public List<TourAdminListResponse> list(Boolean active, Long categoryId, String q) {
          String normQ = (q == null || q.isBlank()) ? null : q.trim();
          List<Tour> tours = tourRepository.findForAdmin(active, categoryId, normQ);
          return tours.stream().map(this::toListRes).toList();
     }

     @Transactional(readOnly = true)
     public TourAdminDetailResponse get(Long id) {
          Objects.requireNonNull(id, "id");
          Tour t = tourRepository.findDetailById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour"));
          return toDetailRes(t);
     }

     @Transactional
     public TourAdminDetailResponse create(TourUpsertRequest req) {
          validate(req, null);

          Tour t = new Tour();
          apply(t, req);

          return toDetailRes(tourRepository.save(t));
     }

     @Transactional
     public TourAdminDetailResponse update(Long id, TourUpsertRequest req) {
          Objects.requireNonNull(id, "id");
          validate(req, id);

          Tour t = tourRepository.findDetailById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour"));

          apply(t, req);
          return toDetailRes(tourRepository.save(t));
     }

     @Transactional
     public TourAdminDetailResponse toggle(Long id) {
          Objects.requireNonNull(id, "id");
          Tour t = tourRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour"));
          t.setIsActive(t.getIsActive() == null || !t.getIsActive());
          return toDetailRes(tourRepository.save(t));
     }

     private void validate(TourUpsertRequest req, Long id) {
          if (req == null)
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
          if (req.getTitle() == null || req.getTitle().trim().isEmpty())
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiêu đề không được rỗng");
          if (req.getTourLineId() == null)
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn dòng tour");
          if (req.getTransportModeId() == null)
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn phương tiện");
          if (req.getDurationDays() == null || req.getDurationDays() <= 0)
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời gian không hợp lệ");
          if (req.getDurationNights() == null || req.getDurationNights() < 0)
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số đêm không hợp lệ");

          String code = norm(req.getCode());
          if (code == null)
               code = genCode(req);
          if (id == null) {
               if (tourRepository.existsByCode(code))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã tour đã tồn tại");
          } else {
               if (tourRepository.existsByCodeAndIdNot(code, id))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã tour đã tồn tại");
          }

          String slug = norm(req.getSlug());
          if (slug == null)
               slug = slugify(req.getTitle());
          if (id == null) {
               if (tourRepository.existsBySlug(slug))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
          } else {
               if (tourRepository.existsBySlugAndIdNot(slug, id))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
          }
     }

     private void apply(Tour t, TourUpsertRequest req) {
          String code = norm(req.getCode());
          if (code == null)
               code = genCode(req);
          String slug = norm(req.getSlug());
          if (slug == null)
               slug = slugify(req.getTitle());

          t.setCode(code);
          t.setTitle(req.getTitle().trim());
          t.setSlug(slug);

          TourLine line = tourLineRepository.findById(req.getTourLineId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy dòng tour"));
          TransportMode mode = transportModeRepository.findById(req.getTransportModeId())
                    .orElseThrow(
                              () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy phương tiện"));
          t.setTourLine(line);
          t.setTransportMode(mode);

          t.setDurationDays(req.getDurationDays());
          t.setDurationNights(req.getDurationNights());
          t.setBasePrice(req.getBasePrice());

          if (req.getStartLocationId() != null) {
               Destination start = destinationRepository.findById(req.getStartLocationId())
                         .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                   "Không tìm thấy điểm khởi hành"));
               t.setStartLocation(start);
          } else {
               t.setStartLocation(null);
          }

          t.setSummary(null);
          t.setOverviewHtml(req.getOverviewHtml());
          t.setAdditionalInfoHtml(req.getAdditionalInfoHtml());
          t.setNotesHtml(req.getNotesHtml());
          t.setIsActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive());

          Set<TourCategory> cats = new HashSet<>();
          List<Long> catIds = sanitizeIds(req.getCategoryIds());
          if (!catIds.isEmpty()) {
               cats.addAll(tourCategoryRepository.findAllById(catIds));
          }
          t.setCategories(cats);

          Set<Destination> dests = new HashSet<>();
          List<Long> destIds = sanitizeIds(req.getDestinationIds());
          if (!destIds.isEmpty()) {
               dests.addAll(destinationRepository.findAllById(destIds));
          }
          t.setDestinations(dests);

          List<TourImage> images = buildImages(t, req.getImages());
          t.getImages().clear();
          t.getImages().addAll(images);

          List<ItineraryDay> days = buildItineraries(t, req.getItineraryDays());
          if (t.getId() != null) {
               itineraryDayRepository.deleteByTourId(t.getId());
               itineraryDayRepository.flush();
          }
          t.getItineraryDays().clear();
          t.getItineraryDays().addAll(days);
     }

     private List<TourImage> buildImages(Tour tour, List<TourImageDto> items) {
          if (items == null)
               return new ArrayList<>();
          List<TourImageDto> sorted = items.stream()
                    .filter(i -> i.getUrl() != null && !i.getUrl().isBlank())
                    .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? 0 : i.getSortOrder()))
                    .toList();

          List<TourImage> out = new ArrayList<>();
          int order = 0;
          for (TourImageDto i : sorted) {
               TourImage img = new TourImage();
               img.setTour(tour);
               img.setImageUrl(i.getUrl().trim());
               img.setIsThumbnail(Boolean.TRUE.equals(i.getIsThumbnail()));
               img.setSortOrder(i.getSortOrder() != null ? i.getSortOrder() : order);
               out.add(img);
               order++;
          }
          return out;
     }

     private List<ItineraryDay> buildItineraries(Tour tour, List<ItineraryDayDto> items) {
          if (items == null)
               return new ArrayList<>();
          List<ItineraryDayDto> sorted = items.stream()
                    .filter(i -> i.getTitleRoute() != null && !i.getTitleRoute().isBlank())
                    .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? 0 : i.getSortOrder()))
                    .toList();

          List<ItineraryDay> out = new ArrayList<>();
          for (ItineraryDayDto i : sorted) {
               ItineraryDay d = new ItineraryDay();
               d.setTour(tour);
               d.setDayNo(i.getDayNo() != null ? i.getDayNo() : 1);
               d.setTitleRoute(i.getTitleRoute().trim());
               d.setMeals(i.getMeals());
               d.setContentHtml(i.getContentHtml() == null ? "" : i.getContentHtml());
               d.setSortOrder(i.getSortOrder() != null ? i.getSortOrder() : 0);
               out.add(d);
          }
          return out;
     }

     private TourAdminListResponse toListRes(Tour t) {
          TourAdminListResponse r = new TourAdminListResponse();
          r.setId(t.getId());
          r.setCode(t.getCode());
          r.setTitle(t.getTitle());
          r.setDurationDays(t.getDurationDays());
          r.setDurationNights(t.getDurationNights());
          r.setBasePrice(t.getBasePrice());
          r.setIsActive(t.getIsActive());
          r.setTourLineName(t.getTourLine() != null ? t.getTourLine().getName() : null);
          r.setTransportModeName(t.getTransportMode() != null ? t.getTransportMode().getName() : null);
          r.setStartLocationName(t.getStartLocation() != null ? t.getStartLocation().getName() : null);
          r.setCategories(t.getCategories() == null ? List.of()
                    : t.getCategories().stream().map(TourCategory::getName).toList());
          r.setDestinationNames(t.getDestinations() == null ? List.of()
                    : t.getDestinations().stream().map(Destination::getName).toList());

          String thumb = null;
          if (t.getImages() != null) {
               for (TourImage img : t.getImages()) {
                    if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                         thumb = img.getImageUrl();
                         break;
                    }
               }
               if (thumb == null && !t.getImages().isEmpty())
                    thumb = t.getImages().get(0).getImageUrl();
          }
          r.setThumbnailUrl(thumb);
          return r;
     }

     private TourAdminDetailResponse toDetailRes(Tour t) {
          TourAdminDetailResponse r = new TourAdminDetailResponse();
          r.setId(t.getId());
          r.setCode(t.getCode());
          r.setTitle(t.getTitle());
          r.setSlug(t.getSlug());
          r.setTourLineId(t.getTourLine() != null ? t.getTourLine().getId() : null);
          r.setTransportModeId(t.getTransportMode() != null ? t.getTransportMode().getId() : null);
          r.setDurationDays(t.getDurationDays());
          r.setDurationNights(t.getDurationNights());
          r.setStartLocationId(t.getStartLocation() != null ? t.getStartLocation().getId() : null);
          r.setStartLocationName(t.getStartLocation() != null ? t.getStartLocation().getName() : null);
          r.setBasePrice(t.getBasePrice());
          r.setSummary(t.getSummary());
          r.setOverviewHtml(t.getOverviewHtml());
          r.setAdditionalInfoHtml(t.getAdditionalInfoHtml());
          r.setNotesHtml(t.getNotesHtml());
          r.setIsActive(t.getIsActive());

          r.setCategoryIds(t.getCategories() == null ? List.of()
                    : t.getCategories().stream().map(TourCategory::getId).toList());
          r.setDestinationIds(t.getDestinations() == null ? List.of()
                    : t.getDestinations().stream().map(Destination::getId).toList());
          r.setDestinationNames(t.getDestinations() == null ? List.of()
                    : t.getDestinations().stream().map(Destination::getName).toList());

          List<TourImageDto> images = new ArrayList<>();
          if (t.getImages() != null) {
               for (TourImage img : t.getImages()) {
                    TourImageDto d = new TourImageDto();
                    d.setUrl(img.getImageUrl());
                    d.setIsThumbnail(img.getIsThumbnail());
                    d.setSortOrder(img.getSortOrder());
                    images.add(d);
               }
          }
          r.setImages(images);

          List<ItineraryDayDto> days = new ArrayList<>();
          if (t.getItineraryDays() != null) {
               for (ItineraryDay day : t.getItineraryDays()) {
                    ItineraryDayDto d = new ItineraryDayDto();
                    d.setDayNo(day.getDayNo());
                    d.setTitleRoute(day.getTitleRoute());
                    d.setMeals(day.getMeals());
                    d.setContentHtml(day.getContentHtml());
                    d.setSortOrder(day.getSortOrder());
                    days.add(d);
               }
          }
          r.setItineraryDays(days);
          return r;
     }

     private String norm(String s) {
          if (s == null)
               return null;
          String out = s.trim();
          return out.isEmpty() ? null : out;
     }

     private List<Long> sanitizeIds(List<Long> ids) {
          if (ids == null || ids.isEmpty())
               return List.of();
          return ids.stream().filter(Objects::nonNull).toList();
     }

     private String genCode(TourUpsertRequest req) {
          String slug = slugify(req.getTitle());
          String prefix = buildPrefixFromCategories(req.getCategoryIds());
          if (prefix == null)
               prefix = buildPrefix(slug);
          String loc = buildLocationCode(slug);
          int num = (int) (System.currentTimeMillis() % 9000) + 1000;
          return (prefix + loc + num).toUpperCase();
     }

     private String buildPrefixFromCategories(List<Long> categoryIds) {
          List<Long> ids = sanitizeIds(categoryIds);
          if (ids.isEmpty())
               return null;
          List<TourCategory> cats = tourCategoryRepository.findAllById(ids);
          for (TourCategory c : cats) {
               String slug = c.getSlug() == null ? "" : c.getSlug().toLowerCase();
               String name = c.getName() == null ? "" : c.getName().toLowerCase();
               if (slug.contains("noi-dia") || name.contains("trong nuoc"))
                    return "ND";
               if (slug.contains("nuoc-ngoai") || name.contains("ngoai nuoc"))
                    return "NN";
          }
          return null;
     }

     private String buildPrefix(String slug) {
          if (slug == null)
               return "TR";
          if (slug.contains("noi-dia"))
               return "ND";
          if (slug.contains("nuoc-ngoai"))
               return "NN";
          String[] parts = slug.split("-");
          for (String p : parts) {
               if (p == null || p.isBlank())
                    continue;
               if (p.matches("\\d+"))
                    continue;
               String up = p.toUpperCase();
               return up.length() >= 2 ? up.substring(0, 2) : up + "X";
          }
          return "TR";
     }

     private String buildLocationCode(String slug) {
          if (slug == null || slug.isBlank())
               return "TOU";
          if (slug.contains("da-nang"))
               return "DNG";
          if (slug.contains("ha-noi"))
               return "HAN";
          if (slug.contains("ho-chi-minh") || slug.contains("sai-gon") || slug.contains("tp-hcm")
                    || slug.contains("tp-ho-chi-minh") || slug.contains("hcm"))
               return "SGN";

          String[] parts = slug.split("-");
          List<String> words = new ArrayList<>();
          for (String p : parts) {
               if (p == null || p.isBlank())
                    continue;
               if (p.matches("\\d+"))
                    continue;
               if (p.equals("noi") || p.equals("dia") || p.equals("nuoc") || p.equals("ngoai")
                         || p.equals("tour") || p.equals("du") || p.equals("lich"))
                    continue;
               words.add(p);
          }
          if (words.isEmpty())
               return "TOU";
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < words.size() && sb.length() < 3; i++) {
               sb.append(words.get(i).substring(0, 1));
          }
          while (sb.length() < 3)
               sb.append('X');
          return sb.toString().toUpperCase();
     }

     private String slugify(String s) {
          if (s == null)
               return "";
          String out = Normalizer.normalize(s, Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
          out = out.replace('đ', 'd').replace('Đ', 'd');
          return out.trim().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "")
                    .replaceAll("-+", "-");
     }
}
