package com.vietravel.booking.domain.repository.tour;

import com.vietravel.booking.domain.entity.tour.Wishlist;
import com.vietravel.booking.domain.entity.tour.WishlistId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

     long countByUserId(Long userId);

     boolean existsByUserIdAndTourId(Long userId, Long tourId);

     void deleteByUserIdAndTourId(Long userId, Long tourId);

     @Query("""
               select w.tour.id
               from Wishlist w
               where w.user.id = :userId
               """)
     List<Long> findTourIdsByUserId(@Param("userId") Long userId);

     @Query("""
               select distinct w
               from Wishlist w
               join fetch w.tour t
               left join fetch t.images
               where w.user.id = :userId
               order by w.createdAt desc
               """)
     List<Wishlist> findByUserIdWithTour(@Param("userId") Long userId, Pageable pageable);
}
