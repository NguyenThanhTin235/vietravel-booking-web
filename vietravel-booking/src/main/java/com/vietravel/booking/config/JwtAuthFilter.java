package com.vietravel.booking.config;

import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthFilter(JwtService jwtService, UserAccountRepository userAccountRepository) {
        this.jwtService = jwtService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;

        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            var cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("accessToken".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
        }

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtService.parseClaims(token);
            String email = claims.get("email") != null ? String.valueOf(claims.get("email")) : null;
            String role = claims.get("role") != null ? String.valueOf(claims.get("role")) : null;

            var user = resolveUserFromClaims(email, claims.get("sub"));
            if (user != null) {
                email = user.getEmail();
                role = user.getRole().name();
            }

            if (role == null || role.isBlank() || "null".equalsIgnoreCase(role)) {
                filterChain.doFilter(request, response);
                return;
            }

            String principal = (email == null || email.isBlank() || "null".equalsIgnoreCase(email))
                    ? String.valueOf(claims.get("sub"))
                    : email;
            var authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/")
                || path.equals("/favicon.ico")
                || path.startsWith("/auth")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/vendor")
                || path.equals("/health");
    }

    private com.vietravel.booking.domain.entity.auth.UserAccount resolveUserFromClaims(String email, Object sub) {
        if (email != null && !email.isBlank() && !"null".equalsIgnoreCase(email)) {
            var user = userAccountRepository.findByEmail(email).orElse(null);
            if (user != null) {
                return user;
            }
        }

        if (sub != null) {
            try {
                Long id = Long.parseLong(String.valueOf(sub));
                return userAccountRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
