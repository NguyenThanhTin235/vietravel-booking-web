package com.vietravel.booking.config;

import com.vietravel.booking.service.tour.ElasticsearchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchInitializer {

    private final ElasticsearchSyncService elasticsearchSyncService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        log.info("Initializing Elasticsearch data...");
        try {
            elasticsearchSyncService.syncAllTours();
            elasticsearchSyncService.syncAllNews();
            log.info("Elasticsearch data initialization completed.");
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch data. Make sure Elasticsearch is running at http://localhost:9200", e);
        }
    }
}
