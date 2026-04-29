package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.TourLine;
import com.vietravel.booking.domain.entity.tour.TransportMode;
import com.vietravel.booking.domain.repository.tour.DestinationRepository;
import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import com.vietravel.booking.domain.repository.tour.TourLineRepository;
import com.vietravel.booking.domain.repository.tour.TransportModeRepository;
import com.vietravel.booking.service.tour.TourPublicService;
import com.vietravel.booking.web.dto.tour.TourCalendarMonth;
import com.vietravel.booking.web.dto.tour.TourPublicDetailView;
import com.vietravel.booking.web.dto.tour.TourPublicListItem;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/tour")
public class TourController {

     private final TourPublicService tourPublicService;
     private final TourLineRepository tourLineRepository;
     private final DestinationRepository destinationRepository;
     private final TourCategoryRepository tourCategoryRepository;
     private final TransportModeRepository transportModeRepository;

     public TourController(
               TourPublicService tourPublicService,
               TourLineRepository tourLineRepository,
               DestinationRepository destinationRepository,
               TourCategoryRepository tourCategoryRepository,
               TransportModeRepository transportModeRepository) {
          this.tourPublicService = tourPublicService;
          this.tourLineRepository = tourLineRepository;
          this.destinationRepository = destinationRepository;
          this.tourCategoryRepository = tourCategoryRepository;
          this.transportModeRepository = transportModeRepository;
     }

     @GetMapping("/tim-kiem")
     public String search(
               @RequestParam(value = "q", required = false) String q,
               @RequestParam(value = "categoryId", required = false) Long categoryId,
               @RequestParam(value = "tourLineId", required = false) Long tourLineId,
               @RequestParam(value = "startLocationId", required = false) Long startLocationId,
               @RequestParam(value = "destinationId", required = false) Long destinationId,
               @RequestParam(value = "transportModeId", required = false) Long transportModeId,
               @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
               @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
               @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
               @RequestParam(value = "sort", required = false, defaultValue = "nearest") String sort,
               @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
               Model model) {
          int pageIndex = Math.max(0, (page == null ? 1 : page) - 1);
          int size = 10;

          Page<TourPublicListItem> result = tourPublicService.search(
                    q,
                    categoryId,
                    tourLineId,
                    startLocationId,
                    destinationId,
                    transportModeId,
                    minPrice,
                    maxPrice,
                    date,
                    sort,
                    pageIndex,
                    size);

          model.addAttribute("pageTitle", "Tìm kiếm tour");
          model.addAttribute("activeNav", "destination");

          model.addAttribute("q", q);
          model.addAttribute("categoryId", categoryId);
          model.addAttribute("tourLineId", tourLineId);
          model.addAttribute("startLocationId", startLocationId);
          model.addAttribute("destinationId", destinationId);
          model.addAttribute("transportModeId", transportModeId);
          model.addAttribute("minPrice", minPrice);
          model.addAttribute("maxPrice", maxPrice);
          model.addAttribute("date", date);
          model.addAttribute("sort", sort);

          model.addAttribute("tours", result.getContent());
          model.addAttribute("totalElements", result.getTotalElements());
          model.addAttribute("totalPages", result.getTotalPages());
          model.addAttribute("currentPage", pageIndex + 1);

          List<Integer> pageNumbers = new ArrayList<>();
          for (int i = 1; i <= result.getTotalPages(); i++) {
               pageNumbers.add(i);
          }
          model.addAttribute("pageNumbers", pageNumbers);

          List<TourLine> tourLines = tourLineRepository.findByIsActiveTrueOrderBySortOrderAsc();
          model.addAttribute("tourLines", tourLines);

          model.addAttribute("tourCategories", tourCategoryRepository.findAllByIsActiveTrueOrderBySortOrderAscIdAsc());

          List<TransportMode> transportModes = transportModeRepository.findByIsActiveTrueOrderBySortOrderAsc();
          model.addAttribute("transportModes", transportModes);

          List<Destination> destinations = destinationRepository.findAll().stream()
                    .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                    .sorted(Comparator.comparing(Destination::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                              .thenComparing(Destination::getName, Comparator.nullsLast(String::compareTo)))
                    .toList();
          model.addAttribute("destinations", destinations);

          String activeBudget = resolveBudgetKey(minPrice, maxPrice);
          model.addAttribute("activeBudget", activeBudget);
          model.addAttribute("budgetOptions", budgetOptions());

          return "public/tours/list";
     }

     private String resolveBudgetKey(BigDecimal minPrice, BigDecimal maxPrice) {
          if (minPrice == null && maxPrice == null) {
               return "";
          }
          for (BudgetOption opt : budgetOptions()) {
               if (Objects.equals(opt.minPrice(), minPrice) && Objects.equals(opt.maxPrice(), maxPrice)) {
                    return opt.key();
               }
          }
          return "";
     }

     private List<BudgetOption> budgetOptions() {
          return List.of(
                    new BudgetOption("under5", "Dưới 5 triệu", null, new BigDecimal("5000000")),
                    new BudgetOption("5to10", "Từ 5 - 10 triệu", new BigDecimal("5000000"), new BigDecimal("10000000")),
                    new BudgetOption("10to20", "Từ 10 - 20 triệu", new BigDecimal("10000000"),
                              new BigDecimal("20000000")),
                    new BudgetOption("over20", "Trên 20 triệu", new BigDecimal("20000000"), null));
     }

     private record BudgetOption(String key, String label, BigDecimal minPrice, BigDecimal maxPrice) {
     }

     @GetMapping("/{slug}")
     public String detail(@PathVariable("slug") String slug, Model model) {
          TourPublicDetailView detail = tourPublicService.getDetailBySlug(slug);
          List<TourCalendarMonth> months = tourPublicService.buildCalendar(detail.getId(), 6);
          List<TourPublicListItem> related = tourPublicService.relatedToursBySlug(slug, 3);

          model.addAttribute("pageTitle", detail.getTitle());
          model.addAttribute("activeNav", "destination");
          model.addAttribute("detail", detail);
          model.addAttribute("calendarMonths", months);
          model.addAttribute("relatedTours", related);

          return "public/tours/detail";
     }

     @GetMapping("/{slug}/dat")
     public String booking(
               @PathVariable("slug") String slug,
               @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
               @RequestParam(value = "adultPrice", required = false) BigDecimal adultPrice,
               @RequestParam(value = "childPrice", required = false) BigDecimal childPrice,
               Model model) {
          TourPublicDetailView detail = tourPublicService.getDetailBySlug(slug);
          BigDecimal resolvedAdult = adultPrice != null ? adultPrice : detail.getBasePrice();
          BigDecimal resolvedChild = childPrice != null ? childPrice : null;

          model.addAttribute("pageTitle", "Đặt tour");
          model.addAttribute("activeNav", "destination");
          model.addAttribute("detail", detail);
          model.addAttribute("selectedDate", date);
          model.addAttribute("adultPrice", resolvedAdult);
          model.addAttribute("childPrice", resolvedChild);

          return "public/bookings/checkout";
     }
}
