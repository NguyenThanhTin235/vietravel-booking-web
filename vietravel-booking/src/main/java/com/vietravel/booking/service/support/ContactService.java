package com.vietravel.booking.service.support;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.support.ContactInquiry;
import com.vietravel.booking.domain.entity.support.ContactInquiryStatus;
import com.vietravel.booking.domain.entity.support.NotificationType;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.repository.support.ContactInquiryRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactService {

     private final ContactInquiryRepository contactInquiryRepository;
     private final UserAccountRepository userAccountRepository;
     private final MailService mailService;
     private final NotificationService notificationService;

     public ContactService(ContactInquiryRepository contactInquiryRepository,
               UserAccountRepository userAccountRepository,
               MailService mailService,
               NotificationService notificationService) {
          this.contactInquiryRepository = contactInquiryRepository;
          this.userAccountRepository = userAccountRepository;
          this.mailService = mailService;
          this.notificationService = notificationService;
     }

     @Transactional
     public ContactInquiry createInquiry(ContactInquiry inquiry) {
          if (inquiry == null) {
               return null;
          }
          normalizeInquiry(inquiry);
          if (inquiry.getStatus() == null) {
               inquiry.setStatus(ContactInquiryStatus.NEW);
          }
          if (inquiry.getUser() == null) {
               inquiry.setUser(getCurrentUserOrNull());
          }
          return contactInquiryRepository.save(inquiry);
     }

     @Transactional(readOnly = true)
     public List<ContactInquiry> findForStaff(ContactInquiryStatus status, String q) {
          return contactInquiryRepository.findForStaff(status, q);
     }

     @Transactional(readOnly = true)
     public ContactInquiry getById(Long id) {
          return id == null ? null : contactInquiryRepository.findById(id).orElse(null);
     }

     @Transactional
     public boolean markRead(Long id) {
          ContactInquiry inquiry = contactInquiryRepository.findById(id).orElse(null);
          if (inquiry == null) {
               return false;
          }
          inquiry.setStatus(ContactInquiryStatus.READ);
          contactInquiryRepository.save(inquiry);
          return true;
     }

     @Transactional
     public boolean replyToInquiry(Long id, String subject, String message) {
          ContactInquiry inquiry = contactInquiryRepository.findById(id).orElse(null);
          if (inquiry == null) {
               return false;
          }
          String email = inquiry.getEmail();
          String name = inquiry.getFullName() != null ? inquiry.getFullName() : "Quý khách";
          String finalSubject = subject == null || subject.isBlank()
                    ? "Phản hồi: " + (inquiry.getSubject() != null ? inquiry.getSubject() : "Liên hệ")
                    : subject.trim();
          String finalMessage = message == null ? "" : message.trim();
          if (email == null || email.isBlank() || finalMessage.isEmpty()) {
               return false;
          }
          mailService.sendContactReply(email, finalSubject, finalMessage, name);
          appendReplyHistory(inquiry, finalSubject, finalMessage);
          inquiry.setStatus(ContactInquiryStatus.RESOLVED);
          inquiry.setHandledBy(getCurrentUserOrNull());
          inquiry.setLastRepliedAt(LocalDateTime.now());
          inquiry.setReplyCount((inquiry.getReplyCount() != null ? inquiry.getReplyCount() : 0) + 1);
          contactInquiryRepository.save(inquiry);

          if (inquiry.getUser() != null) {
               notificationService.createForUser(
                         inquiry.getUser(),
                         "Đã phản hồi liên hệ",
                         "Chúng tôi đã phản hồi yêu cầu: " + finalSubject,
                         NotificationType.SUCCESS,
                         "/contact?sent=success");
          }
          return true;
     }

     private void appendReplyHistory(ContactInquiry inquiry, String subject, String message) {
          if (inquiry == null) {
               return;
          }
          StringBuilder sb = new StringBuilder();
          if (inquiry.getReplyHistory() != null && !inquiry.getReplyHistory().isBlank()) {
               sb.append(inquiry.getReplyHistory().trim()).append("\n\n");
          }
          String time = LocalDateTime.now().toString();
          sb.append("[").append(time).append("] ").append(subject).append("\n");
          sb.append(message);
          inquiry.setReplyHistory(sb.toString());
     }

     private void normalizeInquiry(ContactInquiry inquiry) {
          inquiry.setInfoType(trimOrEmpty(inquiry.getInfoType()));
          inquiry.setFullName(trimOrEmpty(inquiry.getFullName()));
          inquiry.setEmail(trimOrEmpty(inquiry.getEmail()));
          inquiry.setPhone(trimOrEmpty(inquiry.getPhone()));
          inquiry.setCompanyName(trimOrNull(inquiry.getCompanyName()));
          inquiry.setAddress(trimOrNull(inquiry.getAddress()));
          inquiry.setSubject(trimOrEmpty(inquiry.getSubject()));
          inquiry.setContent(trimOrEmpty(inquiry.getContent()));
          if (inquiry.getGuestCount() == null) {
               inquiry.setGuestCount(0);
          }
     }

     private String trimOrEmpty(String value) {
          return value == null ? "" : value.trim();
     }

     private String trimOrNull(String value) {
          if (value == null) {
               return null;
          }
          String trimmed = value.trim();
          return trimmed.isEmpty() ? null : trimmed;
     }

     private UserAccount getCurrentUserOrNull() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
               return null;
          }
          String principal = auth.getName();
          if (principal != null && !principal.isBlank()) {
               UserAccount byEmail = userAccountRepository.findByEmail(principal).orElse(null);
               if (byEmail != null) {
                    return byEmail;
               }
               try {
                    Long id = Long.parseLong(principal);
                    return userAccountRepository.findById(id).orElse(null);
               } catch (NumberFormatException ignored) {
                    return null;
               }
          }
          return null;
     }
}
