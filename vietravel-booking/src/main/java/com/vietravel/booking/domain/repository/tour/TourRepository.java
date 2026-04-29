package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.Tour;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "categories",
            "destinations",
            "images"
    })
    @Query("select t from Tour t where t.slug = :slug")
    Optional<Tour> findDetailBySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                            select distinct t
                            from Tour t
                            left join t.destinations d
                            left join t.categories c
                            where t.isActive = true
                                    and (
                                                    :q is null
                                                    or lower(t.title) like lower(concat('%', :q, '%'))
                                                    or lower(t.code) like lower(concat('%', :q, '%'))
                                                    or lower(d.name) like lower(concat('%', :q, '%'))
                                    )
                                    and (:categoryId is null or c.id = :categoryId or c.parent.id = :categoryId)
                                    and (:tourLineId is null or t.tourLine.id = :tourLineId)
                                    and (:startLocationId is null or t.startLocation.id = :startLocationId)
                                    and (:destinationId is null or d.id = :destinationId)
                                    and (:transportModeId is null or t.transportMode.id = :transportModeId)
                                    and (:minPrice is null or t.basePrice >= :minPrice)
                                    and (:maxPrice is null or t.basePrice <= :maxPrice)
                                    and (:tourIds is null or t.id in :tourIds)
            """)
    List<Tour> findForPublic(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("tourLineId") Long tourLineId,
            @Param("startLocationId") Long startLocationId,
            @Param("destinationId") Long destinationId,
            @Param("transportModeId") Long transportModeId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("tourIds") List<Long> tourIds);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                    select distinct t
                    from Tour t
                    left join t.categories c
                    left join t.destinations d
                    where t.isActive = true
                            and t.id <> :id
                            and (
                                            :destinationId is null
                                            or d.id = :destinationId
                            )
                            and (
                                            :categoryId is null
                                            or c.id = :categoryId
                                            or c.parent.id = :categoryId
                            )
                    order by t.updatedAt desc, t.id desc
            """)
    List<Tour> findRelatedPublic(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("destinationId") Long destinationId,
            Pageable pageable);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                select distinct t
                from Tour t
                left join t.destinations d
                where t.isActive = true
                    and t.id <> :id
                    and (:destinationId is null or d.id = :destinationId)
                order by t.updatedAt desc, t.id desc
            """)
    List<Tour> findRelatedByDestination(
            @Param("id") Long id,
            @Param("destinationId") Long destinationId,
            Pageable pageable);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                select distinct t
                from Tour t
                left join t.categories c
                where t.isActive = true
                    and t.id <> :id
                    and (
                        :categoryId is null
                        or c.id = :categoryId
                        or c.parent.id = :categoryId
                    )
                order by t.updatedAt desc, t.id desc
            """)
    List<Tour> findRelatedByCategory(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                select t
                from Tour t
                where t.isActive = true
                    and t.id <> :id
                    and (:tourLineId is null or t.tourLine.id = :tourLineId)
                order by t.updatedAt desc, t.id desc
            """)
    List<Tour> findRelatedByTourLine(
            @Param("id") Long id,
            @Param("tourLineId") Long tourLineId,
            Pageable pageable);

    @EntityGraph(attributePaths = {
            "tourLine",
            "transportMode",
            "startLocation",
            "destinations",
            "images"
    })
    @Query("""
                select t
                from Tour t
                where t.isActive = true
                    and t.id <> :id
                order by t.updatedAt desc, t.id desc
            """)
    List<Tour> findRelatedAll(
            @Param("id") Long id,
            Pageable pageable);
}
