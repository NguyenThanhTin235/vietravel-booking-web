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
import com.vietravel.booking.domain.elasticsearch.TourDocument;
import com.vietravel.booking.domain.elasticsearch.TourSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
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
     private final TourSearchRepository tourSearchRepository;
     private final ElasticsearchOperations elasticsearchOperations;

     public TourPublicService(
               TourRepository tourRepository,
               DepartureRepository departureRepository,
               TourSearchRepository tourSearchRepository,
               ElasticsearchOperations elasticsearchOperations) {
          this.tourRepository = tourRepository;
          this.departureRepository = departureRepository;
          this.tourSearchRepository = tourSearchRepository;
          this.elasticsearchOperations = elasticsearchOperations;
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

          BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

          // Only active tours
          boolQueryBuilder.must(QueryBuilders.term(t -> t.field("isActive").value(true)));

          // Keyword search
          if (q != null && !q.isBlank()) {
               boolQueryBuilder.must(QueryBuilders.multiMatch(m -> m
                         .fields("title", "summary", "categoryNames", "destinationNames")
                         .query(q)
                         .fuzziness("AUTO")));
          }

          // Filters
          if (categoryId != null) {
               boolQueryBuilder.filter(QueryBuilders.term(t -> t.field("categoryIds").value(categoryId)));
          }
          if (tourLineId != null) {
               boolQueryBuilder.filter(QueryBuilders.term(t -> t.field("tourLineId").value(tourLineId)));
          }
          if (startLocationId != null) {
               boolQueryBuilder.filter(QueryBuilders.term(t -> t.field("startLocationId").value(startLocationId)));
          }
          if (destinationId != null) {
               boolQueryBuilder.filter(QueryBuilders.term(t -> t.field("destinationIds").value(destinationId)));
          }
          if (transportModeId != null) {
               boolQueryBuilder.filter(QueryBuilders.term(t -> t.field("transportModeId").value(transportModeId)));
          }

          // Price range
          if (minPrice != null || maxPrice != null) {
               boolQueryBuilder.filter(QueryBuilders.range(r -> {
                    r.field("basePrice");
                    if (minPrice != null)
                         r.gte(JsonData.of(minPrice));
                    if (maxPrice != null)
                         r.lte(JsonData.of(maxPrice));
                    return r;
               }));
          }

          // Date filter (requires JPA lookup for now as dates are in Departures)
          if (date != null) {
               List<Long> tourIds = departureRepository.findTourIdsByStartDate(date);
               if (tourIds == null || tourIds.isEmpty()) {
                    return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
               }
               boolQueryBuilder.filter(QueryBuilders.terms(t -> t.field("id").terms(v -> v.value(
                         tourIds.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).collect(Collectors.toList())))));
          }

          NativeQuery query = NativeQuery.builder()
                    .withQuery(boolQueryBuilder.build()._toQuery())
                    .withPageable(PageRequest.of(page, size))
                    .build();

          // Sorting
          applySorting(query, sort);

          SearchHits<TourDocument> searchHits = elasticsearchOperations.search(query, TourDocument.class);
          List<Long> tourIds = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toList());

          // Build upcoming map for the found tours
          Map<Long, List<LocalDate>> upcomingMap = buildUpcomingMapByIds(tourIds, date);

          List<TourPublicListItem> items = searchHits.getSearchHits().stream()
                    .map(hit -> toListItemFromDoc(hit.getContent(), upcomingMap.getOrDefault(hit.getContent().getId(), List.of())))
                    .collect(Collectors.toList());

          return new PageImpl<>(items, PageRequest.of(page, size), searchHits.getTotalHits());
     }

     private void applySorting(NativeQuery query, String sort) {
          String key = (sort == null || sort.isBlank()) ? "newest" : sort;
          switch (key) {
               case "price-asc" -> query.addSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "basePrice"));
               case "price-desc" -> query.addSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "basePrice"));
               case "newest" -> query.addSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
               // "nearest" sorting is complex in ES without departure dates in the doc.
               // For now, we use default relevance or id-desc.
          }
     }

     private Map<Long, List<LocalDate>> buildUpcomingMapByIds(List<Long> tourIds, LocalDate date) {
          if (tourIds == null || tourIds.isEmpty()) {
               return Map.of();
          }
          LocalDate from = date != null ? date : LocalDate.now();
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

     private TourPublicListItem toListItemFromDoc(TourDocument doc, List<LocalDate> dates) {
          TourPublicListItem r = new TourPublicListItem();
          r.setId(doc.getId());
          r.setSlug(doc.getSlug());
          r.setTitle(doc.getTitle());
          r.setCode(doc.getCode());
          r.setDurationDays(doc.getDurationDays());
          r.setDurationNights(doc.getDurationNights());
          r.setBasePrice(doc.getBasePrice());
          r.setTourLineName(doc.getTourLineName());
          r.setStartLocationName(doc.getStartLocationName());
          r.setTransportModeName(doc.getTransportModeName());
          r.setThumbnailUrl(doc.getThumbnailUrl());
          r.setDepartureDates(dates == null ? List.of() : dates);
          return r;
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
               Map<LocalDate, Departure> depMap = new HashMap<>();
               for (Departure d : deps) {
                    if (d.getStartDate() != null) {
                         depMap.put(d.getStartDate(), d);
                    }
               }

               TourCalendarMonth m = new TourCalendarMonth();
               m.setYear(ym.getYear());
               m.setMonth(ym.getMonthValue());
               m.setLabel("Tháng " + ym.getMonthValue() + "/" + ym.getYear());
               m.setDays(buildMonthDaysWithDeparture(ym, depMap));
               res.add(m);
          }
          return res;
     }

     // Hàm mới: buildMonthDaysWithDeparture
     private List<TourCalendarMonth.CalendarDay> buildMonthDaysWithDeparture(YearMonth ym,
               Map<LocalDate, Departure> depMap) {
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

               Departure dep = depMap.get(d);
               if (dep != null && cd.isInMonth()) {
                    BigDecimal price = dep.getPriceAdult();
                    BigDecimal priceChild = dep.getPriceChild();
                    BigDecimal kPrice = price.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
                    cd.setAvailable(true);
                    cd.setPriceAdult(price);
                    cd.setPriceLabel(df.format(kPrice) + "K");
                    if (priceChild != null) {
                         BigDecimal kChildPrice = priceChild.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
                         cd.setPriceChild(priceChild);
                         cd.setPriceChildLabel(df.format(kChildPrice) + "K");
                    }
                    cd.setCapacity(dep.getCapacity());
                    // Lấy số đã đặt thực tế từ DB
                    Integer booked = departureRepository.sumBookedByDepartureId(dep.getId());
                    cd.setBooked(booked != null ? booked : 0);
                    // Slots hiển thị cho khách
                    int bookedInt = booked != null ? booked : 0;
                    int capInt = dep.getCapacity() != null ? dep.getCapacity() : 0;
                    cd.setSlotsTotal(capInt);
                    cd.setSlotsAvailable(Math.max(0, capInt - bookedInt));
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
