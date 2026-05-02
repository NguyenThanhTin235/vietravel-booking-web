package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.tour.Tour;
import com.vietravel.booking.domain.entity.tour.TourImage;
import com.vietravel.booking.domain.entity.tour.Wishlist;
import com.vietravel.booking.domain.entity.tour.WishlistId;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.tour.TourRepository;
import com.vietravel.booking.domain.repository.tour.WishlistRepository;
import com.vietravel.booking.web.dto.tour.WishlistItemResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistService {

     private final WishlistRepository wishlistRepository;
     private final UserAccountRepository userAccountRepository;
     private final TourRepository tourRepository;

     public WishlistService(
               WishlistRepository wishlistRepository,
               UserAccountRepository userAccountRepository,
               TourRepository tourRepository) {
          this.wishlistRepository = wishlistRepository;
          this.userAccountRepository = userAccountRepository;
          this.tourRepository = tourRepository;
     }

     @Transactional(readOnly = true)
     public List<WishlistItemResponse> list(int limit) {
          UserAccount user = getCurrentUser();
          int size = Math.max(1, Math.min(limit, 50));
          return wishlistRepository.findByUserIdWithTour(user.getId(), PageRequest.of(0, size))
                    .stream()
                    .map(this::toResponse)
                    .toList();
     }

     @Transactional(readOnly = true)
     public List<Long> listTourIds() {
          UserAccount user = getCurrentUser();
          return wishlistRepository.findTourIdsByUserId(user.getId());
     }

     @Transactional(readOnly = true)
     public long count() {
          UserAccount user = getCurrentUser();
          return wishlistRepository.countByUserId(user.getId());
     }

     @Transactional
     public boolean toggle(Long tourId) {
          UserAccount user = getCurrentUser();
          if (tourId == null) {
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu tour");
          }
          boolean exists = wishlistRepository.existsByUserIdAndTourId(user.getId(), tourId);
          if (exists) {
               wishlistRepository.deleteByUserIdAndTourId(user.getId(), tourId);
               return false;
          }

          Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour"));
          Wishlist w = new Wishlist();
          w.setId(new WishlistId(user.getId(), tourId));
          w.setUser(user);
          w.setTour(tour);
          w.setCreatedAt(LocalDateTime.now());
          wishlistRepository.save(w);
          return true;
     }

     @Transactional
     public void remove(Long tourId) {
          UserAccount user = getCurrentUser();
          if (tourId == null) {
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu tour");
          }
          wishlistRepository.deleteByUserIdAndTourId(user.getId(), tourId);
     }

     private WishlistItemResponse toResponse(Wishlist w) {
          WishlistItemResponse r = new WishlistItemResponse();
          if (w == null || w.getTour() == null) {
               return r;
          }
          Tour t = w.getTour();
          r.setTourId(t.getId());
          r.setSlug(t.getSlug());
          r.setTitle(t.getTitle());
          r.setBasePrice(t.getBasePrice());
          r.setThumbnailUrl(resolveThumbnail(t.getImages()));
          r.setCreatedAt(w.getCreatedAt());
          return r;
     }

     private String resolveThumbnail(List<TourImage> images) {
          if (images == null || images.isEmpty()) {
               return null;
          }
          for (TourImage img : images) {
               if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    return img.getImageUrl();
               }
          }
          return images.get(0).getImageUrl();
     }

     private UserAccount getCurrentUser() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
               throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
          }
          String principal = auth.getName();
          if (principal != null && !principal.isBlank()) {
               UserAccount byEmail = userAccountRepository.findByEmail(principal).orElse(null);
               if (byEmail != null) {
                    return byEmail;
               }
               try {
                    Long id = Long.parseLong(principal);
                    return userAccountRepository.findById(id)
                              .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                        "Không tìm thấy người dùng"));
               } catch (NumberFormatException ignored) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng");
               }
          }
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng");
     }
}
