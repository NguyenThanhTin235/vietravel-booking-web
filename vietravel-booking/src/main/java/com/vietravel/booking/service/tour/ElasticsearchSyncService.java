package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.domain.elasticsearch.TourDocument;
import com.vietravel.booking.domain.elasticsearch.TourSearchRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.domain.entity.content.News;
import com.vietravel.booking.domain.elasticsearch.NewsDocument;
import com.vietravel.booking.domain.elasticsearch.NewsSearchRepository;
import com.vietravel.booking.domain.repository.content.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncService {

    private final TourSearchRepository tourSearchRepository;
    private final TourRepository tourRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final NewsRepository newsRepository;
    private final org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;

    public org.springframework.data.elasticsearch.core.ElasticsearchOperations getElasticsearchOperations() {
        return elasticsearchOperations;
    }

    @Transactional(readOnly = true)
    public void syncAllTours() {
        log.info("Starting initial sync of all tours to Elasticsearch");
        tourRepository.findAll().forEach(this::syncTour);
        log.info("Finished initial sync of all tours to Elasticsearch");
    }

    @Transactional(readOnly = true)
    public void syncAllNews() {
        log.info("Starting initial sync of all news to Elasticsearch");
        newsRepository.findAll().forEach(this::syncNews);
        log.info("Finished initial sync of all news to Elasticsearch");
    }

    @Async
    public void syncTourAsync(Long tourId) {
        tourRepository.findById(tourId).ifPresent(this::syncTour);
    }

    public void syncTour(Tour tour) {
        try {
            TourDocument doc = TourDocument.builder()
                    .id(tour.getId())
                    .code(tour.getCode())
                    .title(tour.getTitle())
                    .slug(tour.getSlug())
                    .summary(tour.getSummary())
                    .basePrice(tour.getBasePrice())
                    .durationDays(tour.getDurationDays())
                    .durationNights(tour.getDurationNights())
                    .tourLineName(tour.getTourLine() != null ? tour.getTourLine().getName() : null)
                    .startLocationName(tour.getStartLocation() != null ? tour.getStartLocation().getName() : null)
                    .transportModeName(tour.getTransportMode() != null ? tour.getTransportMode().getName() : null)
                    .thumbnailUrl(resolveThumbnail(tour.getImages()))
                    .tourLineId(tour.getTourLine() != null ? tour.getTourLine().getId() : null)
                    .transportModeId(tour.getTransportMode() != null ? tour.getTransportMode().getId() : null)
                    .startLocationId(tour.getStartLocation() != null ? tour.getStartLocation().getId() : null)
                    .categoryIds(tour.getCategories().stream().map(TourCategory::getId).collect(Collectors.toList()))
                    .destinationIds(tour.getDestinations().stream().map(Destination::getId).collect(Collectors.toList()))
                    .isActive(tour.getIsActive())
                    .categoryNames(tour.getCategories().stream().map(TourCategory::getName).collect(Collectors.toList()))
                    .destinationNames(tour.getDestinations().stream().map(Destination::getName).collect(Collectors.toList()))
                    .build();
            tourSearchRepository.save(doc);
            log.debug("Synced tour {} to Elasticsearch", tour.getId());
        } catch (Exception e) {
            log.error("Failed to sync tour {} to Elasticsearch", tour.getId(), e);
        }
    }

    @Async
    public void deleteTourAsync(Long tourId) {
        try {
            tourSearchRepository.deleteById(tourId);
            log.debug("Deleted tour {} from Elasticsearch", tourId);
        } catch (Exception e) {
            log.error("Failed to delete tour {} from Elasticsearch", tourId, e);
        }
    }

    private String resolveThumbnail(java.util.List<com.vietravel.booking.domain.entity.tour.TourImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        for (com.vietravel.booking.domain.entity.tour.TourImage img : images) {
            if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                return img.getImageUrl();
            }
        }
        return images.get(0).getImageUrl();
    }

    @Async
    public void syncNewsAsync(Long newsId) {
        newsRepository.findById(newsId).ifPresent(this::syncNews);
    }

    public void syncNews(News news) {
        try {
            NewsDocument doc = NewsDocument.builder()
                    .id(news.getId())
                    .title(news.getTitle())
                    .slug(news.getSlug())
                    .thumbnail(news.getThumbnail())
                    .summary(news.getSummary())
                    .status(news.getStatus())
                    .isFeatured(news.getIsFeatured())
                    .build();
            newsSearchRepository.save(doc);
            log.debug("Synced news {} to Elasticsearch", news.getId());
        } catch (Exception e) {
            log.error("Failed to sync news {} to Elasticsearch", news.getId(), e);
        }
    }

    @Async
    public void deleteNewsAsync(Long newsId) {
        try {
            newsSearchRepository.deleteById(newsId);
            log.debug("Deleted news {} from Elasticsearch", newsId);
        } catch (Exception e) {
            log.error("Failed to delete news {} from Elasticsearch", newsId, e);
        }
    }
}
