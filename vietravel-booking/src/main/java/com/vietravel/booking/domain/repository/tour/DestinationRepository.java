package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination,Long>{

    Optional<Destination> findBySlug(String slug);

    // LIST cho admin: fetch luôn category để tránh LazyInitializationException
    @Query("""
    select d
    from Destination d
    join fetch d.category c
    where (:active is null or d.isActive = :active)
      and (
            :categoryId is null
            or c.id = :categoryId
            or c.parent.id = :categoryId
          )
    order by d.sortOrder asc, d.name asc
""")
    List<Destination> findForAdmin(
            @Param("active") Boolean active,
            @Param("categoryId") Long categoryId
    );


    // GET chi tiết (edit form)
    @Query("""
        select d
        from Destination d
        join fetch d.category
        where d.id = :id
    """)
    Optional<Destination> findByIdWithCategory(@Param("id") Long id);
}
