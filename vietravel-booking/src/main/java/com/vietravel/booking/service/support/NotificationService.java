package com.vietravel.booking.service.support;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.support.Notification;
import com.vietravel.booking.domain.entity.support.NotificationType;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.support.NotificationRepository;
import com.vietravel.booking.web.dto.support.NotificationResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

     private final NotificationRepository notificationRepository;
     private final UserAccountRepository userAccountRepository;

     public NotificationService(NotificationRepository notificationRepository,
               UserAccountRepository userAccountRepository) {
          this.notificationRepository = notificationRepository;
          this.userAccountRepository = userAccountRepository;
     }

     @Transactional(readOnly = true)
     public List<NotificationResponse> listCurrent(int limit) {
          UserAccount user = getCurrentUser();
          int size = Math.max(1, Math.min(limit, 50));
          return notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, size))
                    .stream()
                    .map(this::toResponse)
                    .toList();
     }

     @Transactional(readOnly = true)
     public long unreadCount() {
          UserAccount user = getCurrentUser();
          return notificationRepository.countByUserIdAndReadFalse(user.getId());
     }

     @Transactional(readOnly = true)
     public NotificationResponse getDetail(Long id) {
          UserAccount user = getCurrentUser();
          Notification n = notificationRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
          return toResponse(n);
     }

     @Transactional
     public void markRead(Long id) {
          UserAccount user = getCurrentUser();
          Notification n = notificationRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
          if (!n.isRead()) {
               n.setRead(true);
               n.setReadAt(LocalDateTime.now());
               notificationRepository.save(n);
          }
     }

     @Transactional
     public void delete(Long id) {
          UserAccount user = getCurrentUser();
          notificationRepository.deleteByIdAndUserId(id, user.getId());
     }

     @Transactional
     public void createForUser(UserAccount user, String title, String message, NotificationType type, String link) {
          if (user == null || user.getId() == null) {
               return;
          }
          Notification n = new Notification();
          n.setUser(user);
          n.setTitle(title == null ? "" : title.trim());
          n.setMessage(message == null ? "" : message.trim());
          n.setType(type == null ? NotificationType.INFO : type);
          n.setLink(link);
          notificationRepository.save(n);
     }

     @Transactional
     public void createForRole(UserRole role, String title, String message, NotificationType type, String link) {
          if (role == null) {
               return;
          }
          List<UserAccount> users = userAccountRepository.findAllByRole(role);
          if (users == null || users.isEmpty()) {
               return;
          }
          for (UserAccount user : users) {
               createForUser(user, title, message, type, link);
          }
     }

     private NotificationResponse toResponse(Notification n) {
          NotificationResponse r = new NotificationResponse();
          r.setId(n.getId());
          r.setTitle(n.getTitle());
          r.setMessage(n.getMessage());
          r.setType(n.getType());
          r.setLink(n.getLink());
          r.setRead(n.isRead());
          r.setCreatedAt(n.getCreatedAt());
          return r;
     }

     private UserAccount getCurrentUser() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
               throw new RuntimeException("Chưa đăng nhập");
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
                              .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
               } catch (NumberFormatException ignored) {
                    throw new RuntimeException("Không tìm thấy người dùng");
               }
          }
          throw new RuntimeException("Không tìm thấy người dùng");
     }
}
