package com.vietravel.booking.config;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.auth.UserProfile;
import com.vietravel.booking.domain.repository.auth.UserProfileRepository;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.service.auth.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

     private final UserAccountRepository userAccountRepository;
     private final UserProfileRepository userProfileRepository;
     private final JwtService jwtService;

     public GlobalControllerAdvice(UserAccountRepository userAccountRepository,
               UserProfileRepository userProfileRepository,
               JwtService jwtService) {
          this.userAccountRepository = userAccountRepository;
          this.userProfileRepository = userProfileRepository;
          this.jwtService = jwtService;
     }

     @ModelAttribute
     public void addUserInfoToModel(Model model, HttpServletRequest request) {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          Map<String, Object> claims = parseClaimsFromCookie(request);

          UserAccount user = null;
          if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
               String email = auth.getName();
               user = userAccountRepository.findByEmail(email).orElse(null);
          }

          if (user == null) {
               user = resolveUserFromClaims(claims);
          }

          if (user != null) {
               Long userId = user.getId();
               UserProfile profile = userId != null ? userProfileRepository.findById(userId).orElse(null) : null;
               model.addAttribute("currentUser", user);
               model.addAttribute("currentUserProfile", profile);

               String name = (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank())
                         ? profile.getFullName()
                         : user.getEmail();
               model.addAttribute("adminName", name);
               model.addAttribute("adminRole", user.getRole() != null ? user.getRole().name() : "");
               model.addAttribute("adminAvatar", profile != null ? profile.getAvatar() : null);
               model.addAttribute("adminInitial", (name != null && !name.isBlank())
                         ? name.substring(0, 1).toUpperCase()
                         : "A");
               return;
          }

          if (claims != null) {
               String emailFromToken = String.valueOf(claims.get("email"));
               String roleFromToken = String.valueOf(claims.get("role"));
               if (emailFromToken != null && !"null".equalsIgnoreCase(emailFromToken)) {
                    model.addAttribute("adminName", emailFromToken);
                    model.addAttribute("adminInitial", emailFromToken.substring(0, 1).toUpperCase());
               }
               if (roleFromToken != null && !"null".equalsIgnoreCase(roleFromToken)) {
                    model.addAttribute("adminRole", roleFromToken);
               }
          }
     }

     private Map<String, Object> parseClaimsFromCookie(HttpServletRequest request) {
          if (request == null)
               return null;
          Cookie[] cookies = request.getCookies();
          if (cookies == null)
               return null;
          String token = null;
          for (Cookie c : cookies) {
               if ("accessToken".equals(c.getName())) {
                    token = c.getValue();
                    break;
               }
          }
          if (token == null || token.isBlank())
               return null;
          try {
               return jwtService.parseClaims(token);
          } catch (Exception ignored) {
               return null;
          }
     }

     private UserAccount resolveUserFromClaims(Map<String, Object> claims) {
          if (claims == null)
               return null;
          Object emailObj = claims.get("email");
          if (emailObj != null) {
               UserAccount user = userAccountRepository.findByEmail(String.valueOf(emailObj)).orElse(null);
               if (user != null)
                    return user;
          }
          Object subObj = claims.get("sub");
          if (subObj != null) {
               try {
                    Long id = Long.parseLong(String.valueOf(subObj));
                    return userAccountRepository.findById(id).orElse(null);
               } catch (NumberFormatException ignored) {
                    return null;
               }
          }
          return null;
     }
}
