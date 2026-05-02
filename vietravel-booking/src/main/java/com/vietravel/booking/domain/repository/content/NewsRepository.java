package com.vietravel.booking.domain.repository.content;

import com.vietravel.booking.domain.entity.content.News;
import com.vietravel.booking.domain.entity.content.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
     Optional<News> findBySlug(String slug);

     boolean existsBySlug(String slug);

     @Query("select n from News n order by n.createdAt desc")
     List<News> findAllOrderByCreatedAtDesc();

     @Query("select n from News n where n.status = :status order by n.createdAt desc")
     List<News> findByStatusOrderByCreatedAtDesc(@Param("status") NewsStatus status);
}
