package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.Departure;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourImage;
import com.vietravel.booking.domain.repository.tour.DepartureRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.web.dto.tour.TourPublicListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
