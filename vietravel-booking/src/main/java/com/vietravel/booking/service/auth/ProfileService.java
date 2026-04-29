package com.vietravel.booking.service.auth;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.auth.UserProfile;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.auth.UserProfileRepository;
import com.vietravel.booking.domain.entity.support.NotificationType;
import com.vietravel.booking.service.support.NotificationService;
import com.vietravel.booking.web.dto.profile.ProfileResponse;
import com.vietravel.booking.web.dto.profile.ProfileUpdateRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

     private final UserAccountRepository userAccountRepository;
     private final UserProfileRepository userProfileRepository;
     private final NotificationService notificationService;

     public ProfileService(UserAccountRepository userAccountRepository,
               UserProfileRepository userProfileRepository,
               NotificationService notificationService) {
          this.userAccountRepository = userAccountRepository;
          this.userProfileRepository = userProfileRepository;
          this.notificationService = notificationService;
     }

     public ProfileResponse getCurrentProfile() {
          UserAccount user = getCurrentUser();
          Long userId = user.getId();
          if (userId == null) {
               throw new RuntimeException("Không tìm thấy người dùng");
          }
          UserProfile profile = userProfileRepository.findById(userId).orElse(null);
          return toResponse(user, profile);
     }

     @Transactional
     public ProfileResponse updateCurrentProfile(ProfileUpdateRequest req) {
          UserAccount user = getCurrentUser();
          Long userId = user.getId();
          if (userId == null) {
               throw new RuntimeException("Không tìm thấy người dùng");
          }
          UserProfile profile = userProfileRepository.findById(userId).orElse(null);
          if (profile == null) {
               profile = new UserProfile();
               profile.setUser(user);
               user.setProfile(profile);
          }

          if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
               profile.setFullName(req.getFullName().trim());
          } else if (profile.getFullName() == null || profile.getFullName().trim().isEmpty()) {
               throw new RuntimeException("Họ tên không được rỗng");
          }

          profile.setPhone(req.getPhone() == null ? null : req.getPhone().trim());
          profile.setGender(req.getGender() == null ? null : req.getGender().trim());
          profile.setDob(req.getDob());
          profile.setAddress(req.getAddress() == null ? null : req.getAddress().trim());
          profile.setAvatar(req.getAvatar() == null ? null : req.getAvatar().trim());

          userAccountRepository.save(user);

          notificationService.createForUser(
                    user,
                    "Cập nhật thông tin cá nhân",
                    "Thông tin hồ sơ của bạn đã được cập nhật thành công.",
                    NotificationType.SUCCESS,
                    "/profile");
          return toResponse(user, profile);
     }

     private UserAccount getCurrentUser() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
               throw new RuntimeException("Chưa đăng nhập");
          }

          String principal = auth.getName();
          if (principal != null && !principal.isBlank()) {
               var byEmail = userAccountRepository.findByEmail(principal).orElse(null);
               if (byEmail != null)
                    return byEmail;
               try {
                    Long id = Long.parseLong(principal);
                    return userAccountRepository.findById(id)
                              .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
               } catch (NumberFormatException ignored) {
                    throw new RuntimeException("Không tìm thấy người dùng");
               }
          }
          throw new RuntimeException("Không tìm thấy người dùng");
     }

     private ProfileResponse toResponse(UserAccount user, UserProfile profile) {
          ProfileResponse r = new ProfileResponse();
          r.setId(user.getId());
          r.setEmail(user.getEmail());
          if (profile != null) {
               r.setFullName(profile.getFullName());
               r.setPhone(profile.getPhone());
               r.setGender(profile.getGender());
               r.setDob(profile.getDob());
               r.setAddress(profile.getAddress());
               r.setAvatar(profile.getAvatar());
          }
          return r;
     }
}
