package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.ItineraryDay;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourImage;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.tour.TourCalendarMonth;
import com.vietravel.booking.web.dto.tour.TourPublicDetailView;
import com.vietravel.booking.web.dto.tour.TourPublicListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TourPublicService {

     private final TourRepository tourRepository;
     private final DepartureRepository departureRepository;

     public TourPublicService(TourRepository tourRepository, DepartureRepository departureRepository) {
          this.tourRepository = tourRepository;
          this.departureRepository = departureRepository;
     }

     @Transactional(readOnly = true)
     public Page<TourPublicListItem> search(
               String q,
               Long categoryId,
               Long tourLineId,
               Long startLocationId,
               Long destinationId,
               Long transportModeId,
               BigDecimal minPrice,
               BigDecimal maxPrice,
               LocalDate date,
               String sort,
               int page,
               int size) {
          String normQ = (q == null || q.isBlank()) ? null : q.trim();
          List<Long> tourIds = null;
          if (date != null) {
               tourIds = departureRepository.findTourIdsByStartDate(date);
               if (tourIds == null || tourIds.isEmpty()) {
                    return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
               }
          }

          List<Tour> tours = tourRepository.findForPublic(
                    normQ,
                    categoryId,
                    tourLineId,
                    startLocationId,
                    destinationId,
                    transportModeId,
                    minPrice,
                    maxPrice,
                    tourIds);

          Map<Long, List<LocalDate>> upcomingMap = buildUpcomingMap(tours, date);

          Comparator<Tour> comparator = buildComparator(sort, upcomingMap);
          if (comparator != null) {
               tours = tours.stream().sorted(comparator).toList();
          }

          int total = tours.size();
          int from = Math.min(page * size, total);
          int to = Math.min(from + size, total);
          List<Tour> pageTours = from >= to ? List.of() : tours.subList(from, to);

          Map<Long, List<LocalDate>> pageUpcoming = filterUpcomingMap(pageTours, upcomingMap);
          List<TourPublicListItem> items = pageTours.stream()
                    .map(t -> toListItem(t, pageUpcoming.getOrDefault(t.getId(), List.of())))
                    .toList();

          return new PageImpl<>(items, PageRequest.of(page, size), total);
     }

     private Comparator<Tour> buildComparator(String sort, Map<Long, List<LocalDate>> upcomingMap) {
          String key = (sort == null || sort.isBlank()) ? "nearest" : sort;
          return switch (key) {
               case "price-asc" -> Comparator.comparing(Tour::getBasePrice,
                         Comparator.nullsLast(BigDecimal::compareTo));
               case "price-desc" -> Comparator.comparing(Tour::getBasePrice,
                         Comparator.nullsLast(BigDecimal::compareTo)).reversed();
               case "newest" -> Comparator.comparing(Tour::getUpdatedAt,
                         Comparator.nullsLast(Comparator.naturalOrder())).reversed();
               case "nearest" -> Comparator.comparing(
                         t -> firstDate(upcomingMap.get(t.getId())),
                         Comparator.nullsLast(Comparator.naturalOrder()));
               default -> null;
          };
     }

     private LocalDate firstDate(List<LocalDate> dates) {
          return (dates == null || dates.isEmpty()) ? null : dates.get(0);
     }

     private Map<Long, List<LocalDate>> buildUpcomingMap(List<Tour> tours, LocalDate date) {
          if (tours == null || tours.isEmpty()) {
               return Map.of();
          }
          LocalDate from = date != null ? date : LocalDate.now();
          List<Long> tourIds = tours.stream().map(Tour::getId).toList();
          List<Departure> deps = departureRepository.findUpcomingByTourIds(tourIds, from);

          Map<Long, List<LocalDate>> map = new LinkedHashMap<>();
          for (Departure d : deps) {
               if (d.getTour() == null || d.getTour().getId() == null) {
                    continue;
               }
               Long id = d.getTour().getId();
               map.computeIfAbsent(id, k -> new ArrayList<>()).add(d.getStartDate());
          }

          map.replaceAll((k, v) -> v.stream().distinct().collect(Collectors.toList()));
          return map;
     }

     private Map<Long, List<LocalDate>> filterUpcomingMap(List<Tour> pageTours,
               Map<Long, List<LocalDate>> upcomingMap) {
          if (pageTours == null || pageTours.isEmpty()) {
               return Map.of();
          }
          Map<Long, List<LocalDate>> out = new HashMap<>();
          for (Tour t : pageTours) {
               List<LocalDate> dates = upcomingMap.get(t.getId());
               if (dates != null) {
                    out.put(t.getId(), dates);
               }
          }
          return out;
     }

     private TourPublicListItem toListItem(Tour t, List<LocalDate> dates) {
          TourPublicListItem r = new TourPublicListItem();
          r.setId(t.getId());
          r.setSlug(t.getSlug());
          r.setTitle(t.getTitle());
          r.setCode(t.getCode());
          r.setDurationDays(t.getDurationDays());
          r.setDurationNights(t.getDurationNights());
          r.setBasePrice(t.getBasePrice());
          r.setTourLineName(t.getTourLine() != null ? t.getTourLine().getName() : null);
          r.setStartLocationName(t.getStartLocation() != null ? t.getStartLocation().getName() : null);
          r.setTransportModeName(t.getTransportMode() != null ? t.getTransportMode().getName() : null);
          r.setThumbnailUrl(resolveThumbnail(t.getImages()));
          r.setDepartureDates(dates == null ? List.of() : dates);
          return r;
     }

     private String resolveThumbnail(List<TourImage> images) {
          if (images == null || images.isEmpty()) {
               return null;
          }
          for (TourImage img : images) {
               if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    return img.getImageUrl();
               }
          }
          return images.get(0).getImageUrl();
     }

     @Transactional(readOnly = true)
     public TourPublicDetailView getDetailBySlug(String slug) {
          Tour t = tourRepository.findDetailBySlug(slug)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tour"));
          return toDetailView(t);
     }

     @Transactional(readOnly = true)
     public List<TourCalendarMonth> buildCalendar(Long tourId, int months) {
          List<TourCalendarMonth> res = new ArrayList<>();
          if (tourId == null)
               return res;
          YearMonth now = YearMonth.now();
          for (int i = 0; i < months; i++) {
               YearMonth ym = now.plusMonths(i);
               LocalDate from = ym.atDay(1);
               LocalDate to = ym.atEndOfMonth();
               List<Departure> deps = departureRepository.findForCalendar(tourId, from, to);
               Map<LocalDate, BigDecimal> priceMap = new HashMap<>();
               Map<LocalDate, BigDecimal> priceChildMap = new HashMap<>();
               for (Departure d : deps) {
                    if (d.getStartDate() == null || d.getPriceAdult() == null) {
                         continue;
                    }
                    priceMap.merge(d.getStartDate(), d.getPriceAdult(),
                              (a, b) -> a.compareTo(b) <= 0 ? a : b);
                    if (d.getPriceChild() != null) {
                         priceChildMap.merge(d.getStartDate(), d.getPriceChild(),
                                   (a, b) -> a.compareTo(b) <= 0 ? a : b);
                    }
               }

               TourCalendarMonth m = new TourCalendarMonth();
               m.setYear(ym.getYear());
               m.setMonth(ym.getMonthValue());
               m.setLabel("Tháng " + ym.getMonthValue() + "/" + ym.getYear());
               m.setDays(buildMonthDays(ym, priceMap, priceChildMap));
               res.add(m);
          }
          return res;
     }

     private List<TourCalendarMonth.CalendarDay> buildMonthDays(YearMonth ym,
               Map<LocalDate, BigDecimal> priceMap,
               Map<LocalDate, BigDecimal> priceChildMap) {
          List<TourCalendarMonth.CalendarDay> days = new ArrayList<>();
          LocalDate firstDay = ym.atDay(1);
          int shift = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
          if (shift < 0) {
               shift += 7;
          }
          LocalDate gridStart = firstDay.minusDays(shift);

          DecimalFormat df = new DecimalFormat("#,###");
          for (int i = 0; i < 42; i++) {
               LocalDate d = gridStart.plusDays(i);
               TourCalendarMonth.CalendarDay cd = new TourCalendarMonth.CalendarDay();
               cd.setDate(d);
               cd.setDay(d.getDayOfMonth());
               cd.setInMonth(d.getMonthValue() == ym.getMonthValue());

               BigDecimal price = priceMap.get(d);
               BigDecimal priceChild = priceChildMap.get(d);
               if (price != null && cd.isInMonth()) {
                    BigDecimal kPrice = price.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
                    cd.setAvailable(true);
                    cd.setPriceAdult(price);
                    cd.setPriceLabel(df.format(kPrice) + "K");
                    if (priceChild != null) {
                         BigDecimal kChildPrice = priceChild.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
                         cd.setPriceChild(priceChild);
                         cd.setPriceChildLabel(df.format(kChildPrice) + "K");
                    }
               } else {
                    cd.setAvailable(false);
               }

               days.add(cd);
          }
          return days;
     }

     @Transactional(readOnly = true)
     public List<TourPublicListItem> relatedTours(Tour t, int limit) {
          if (t == null || t.getId() == null)
               return List.of();
          int target = Math.max(0, limit);
          if (target == 0) {
               return List.of();
          }

          Long destId = null;
          if (t.getDestinations() != null && !t.getDestinations().isEmpty()) {
               Destination d = t.getDestinations().iterator().next();
               destId = d != null ? d.getId() : null;
          }
          Long categoryId = null;
          if (t.getCategories() != null && !t.getCategories().isEmpty()) {
               categoryId = t.getCategories().iterator().next().getId();
          }
          Long tourLineId = t.getTourLine() != null ? t.getTourLine().getId() : null;

          LinkedHashMap<Long, Tour> picked = new LinkedHashMap<>();
          addRelated(picked, tourRepository.findRelatedByDestination(t.getId(), destId, PageRequest.of(0, target)));
          addRelated(picked, tourRepository.findRelatedByCategory(t.getId(), categoryId, PageRequest.of(0, target)));
          addRelated(picked, tourRepository.findRelatedByTourLine(t.getId(), tourLineId, PageRequest.of(0, target)));
          if (picked.size() < target) {
               addRelated(picked, tourRepository.findRelatedAll(t.getId(), PageRequest.of(0, target)));
          }

          return picked.values().stream().limit(target).map(x -> toListItem(x, List.of())).toList();
     }

     private void addRelated(Map<Long, Tour> picked, List<Tour> tours) {
          if (tours == null || tours.isEmpty()) {
               return;
          }
          for (Tour tour : tours) {
               if (tour == null || tour.getId() == null) {
                    continue;
               }
               picked.putIfAbsent(tour.getId(), tour);
          }
     }

     @Transactional(readOnly = true)
     public List<TourPublicListItem> relatedToursBySlug(String slug, int limit) {
          if (slug == null || slug.isBlank())
               return List.of();
          Tour t = tourRepository.findDetailBySlug(slug).orElse(null);
          return relatedTours(t, limit);
     }

     private TourPublicDetailView toDetailView(Tour t) {
          TourPublicDetailView r = new TourPublicDetailView();
          r.setId(t.getId());
          r.setSlug(t.getSlug());
          r.setTitle(t.getTitle());
          r.setCode(t.getCode());
          r.setDurationDays(t.getDurationDays());
          r.setDurationNights(t.getDurationNights());
          r.setBasePrice(t.getBasePrice());
          r.setTourLineName(t.getTourLine() != null ? t.getTourLine().getName() : null);
          r.setTransportModeName(t.getTransportMode() != null ? t.getTransportMode().getName() : null);
          r.setStartLocationName(t.getStartLocation() != null ? t.getStartLocation().getName() : null);
          r.setOverviewHtml(t.getOverviewHtml());
          r.setAdditionalInfoHtml(t.getAdditionalInfoHtml());
          r.setNotesHtml(t.getNotesHtml());
          r.setImageUrls(
                    t.getImages() == null ? List.of() : t.getImages().stream().map(TourImage::getImageUrl).toList());
          r.setDestinationNames(t.getDestinations() == null ? List.of()
                    : t.getDestinations().stream().map(Destination::getName).toList());
          r.setCategoryNames(t.getCategories() == null ? List.of()
                    : t.getCategories().stream().map(c -> c.getName()).toList());

          List<TourPublicDetailView.ItineraryDayView> days = new ArrayList<>();
          if (t.getItineraryDays() != null) {
               for (ItineraryDay d : t.getItineraryDays()) {
                    TourPublicDetailView.ItineraryDayView v = new TourPublicDetailView.ItineraryDayView();
                    v.setDayNo(d.getDayNo());
                    v.setTitleRoute(d.getTitleRoute());
                    v.setMeals(d.getMeals());
                    v.setContentHtml(d.getContentHtml());
                    days.add(v);
               }
          }
          r.setItineraryDays(days);
          return r;
     }
}
