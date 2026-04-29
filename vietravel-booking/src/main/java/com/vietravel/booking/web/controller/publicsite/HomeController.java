package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourImage;
import com.vietravel.booking.domain.repository.tour.DestinationRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;

    public HomeController(TourRepository tourRepository, DestinationRepository destinationRepository) {
        this.tourRepository = tourRepository;
        this.destinationRepository = destinationRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Vietravel Booking");
        model.addAttribute("activeNav", "home");
        model.addAttribute("featuredTours", tourRepository.findFeaturedPublic(PageRequest.of(0, 4)));
        Set<String> usedImages = new LinkedHashSet<>();
        List<PopularDestinationView> popularDestinations = destinationRepository
                .findPopularDestinations(PageRequest.of(0, 4))
                .stream()
                .map(item -> new PopularDestinationView(
                        item.getDestination(),
                        item.getTotal(),
                        resolveDestinationImage(item.getDestination(), usedImages)))
                .collect(Collectors.toList());
        model.addAttribute("popularDestinations", popularDestinations);
        return "home/index";
    }

    @ResponseBody
    @GetMapping("/health")
    public String health() {
        return "✅ Vietravel Booking is running!";
    }

    @GetMapping("/admin")
    public String homeAdmin(Model model) {
        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("activeSubMenu", "");
        return "admin/index";
    }

    private String resolveDestinationImage(Destination destination, Set<String> usedImages) {
        if (destination == null || destination.getId() == null) {
            return null;
        }
        List<Tour> tours = tourRepository.findTopByDestination(destination.getId(), PageRequest.of(0, 5));
        if (tours.isEmpty()) {
            return null;
        }
        for (Tour tour : tours) {
            String imageUrl = resolveTourThumbnail(tour);
            if (imageUrl != null && !usedImages.contains(imageUrl)) {
                usedImages.add(imageUrl);
                return imageUrl;
            }
        }
        String fallback = resolveTourThumbnail(tours.get(0));
        if (fallback != null) {
            usedImages.add(fallback);
        }
        return fallback;
    }

    private String resolveTourThumbnail(Tour tour) {
        if (tour == null || tour.getImages() == null || tour.getImages().isEmpty()) {
            return null;
        }
        for (TourImage image : tour.getImages()) {
            if (Boolean.TRUE.equals(image.getIsThumbnail())) {
                return image.getImageUrl();
            }
        }
        return tour.getImages().get(0).getImageUrl();
    }

    public static class PopularDestinationView {
        private final Destination destination;
        private final long total;
        private final String imageUrl;

        public PopularDestinationView(Destination destination, long total, String imageUrl) {
            this.destination = destination;
            this.total = total;
            this.imageUrl = imageUrl;
        }

        public Destination getDestination() {
            return destination;
        }

        public long getTotal() {
            return total;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

}
