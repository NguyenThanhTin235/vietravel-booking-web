package com.vietravel.booking.domain.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourSearchRepository extends ElasticsearchRepository<TourDocument, Long> {
}
