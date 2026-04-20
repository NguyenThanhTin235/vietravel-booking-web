package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.Tour;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {

     boolean existsByCode(String code);

     boolean existsBySlug(String slug);

     boolean existsByCodeAndIdNot(String code, Long id);

     boolean existsBySlugAndIdNot(String slug, Long id);

     @Query("""
                   select distinct t
                   from Tour t
                   left join t.categories c
                   where (:active is null or t.isActive = :active)
                     and (
                         :categoryId is null
                         or c.id = :categoryId
                         or c.parent.id = :categoryId
                     )
                     and (
                         :q is null
                         or lower(t.title) like lower(concat('%', :q, '%'))
                         or lower(t.code) like lower(concat('%', :q, '%'))
                     )
                   order by t.updatedAt desc, t.id desc
               """)
     List<Tour> findForAdmin(
               @Param("active") Boolean active,
               @Param("categoryId") Long categoryId,
               @Param("q") String q);

     @EntityGraph(attributePaths = {
               "tourLine",
               "transportMode",
               "startLocation",
               "categories",
               "destinations",
               "images"
     })
     @Query("select t from Tour t where t.id = :id")
     Optional<Tour> findDetailById(@Param("id") Long id);
}
