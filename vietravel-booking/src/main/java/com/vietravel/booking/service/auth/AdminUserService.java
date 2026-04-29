package com.vietravel.booking.service.auth;

import com.vietravel.booking.domain.entity.auth.*;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.entity.support.NotificationType;
import com.vietravel.booking.service.support.NotificationService;
import com.vietravel.booking.web.dto.auth.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AdminUserService(UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public List<AdminUserRowResponse> list(Boolean active, UserRole role, UserStatus status, String q) {
        String key = (q == null || q.trim().isEmpty()) ? null : q.trim();
        return userAccountRepository.findForAdmin(active, role, status, key).stream().map(this::toRow).toList();
    }

    public AdminUserDetailResponse get(Long id) {
        UserAccount u = userAccountRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return toDetail(u);
    }

    @Transactional
    public AdminUserDetailResponse createStaff(AdminStaffCreateRequest req) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty())
            throw new RuntimeException("Email không được rỗng");
        if (req.getPassword() == null || req.getPassword().trim().isEmpty())
            throw new RuntimeException("Mật khẩu không được rỗng");
        if (req.getFullName() == null || req.getFullName().trim().isEmpty())
            throw new RuntimeException("Họ tên không được rỗng");

        String email = req.getEmail().trim().toLowerCase();
        userAccountRepository.findByEmail(email).ifPresent(x -> {
            throw new RuntimeException("Email đã tồn tại");
        });

        UserAccount u = new UserAccount();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        u.setRole(UserRole.STAFF);
        u.setStatus(UserStatus.ACTIVE);

        UserProfile p = new UserProfile();
        p.setUser(u);
        p.setFullName(req.getFullName().trim());
        p.setPhone(req.getPhone() == null ? null : req.getPhone().trim());

        u.setProfile(p);

        userAccountRepository.save(u);

        notificationService.createForUser(
                u,
                "Tài khoản nhân viên được tạo",
                "Tài khoản nhân viên của bạn đã được tạo thành công. Vui lòng đăng nhập để sử dụng hệ thống.",
                NotificationType.INFO,
                "/staff");
        return toDetail(u);
    }

    @Transactional
    public AdminUserDetailResponse update(Long id, AdminUserUpdateRequest req) {
        UserAccount u = userAccountRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (req.getStatus() != null) {
            u.setStatus(req.getStatus());
        }

        UserProfile p = u.getProfile();
        if (p == null) {
            p = new UserProfile();
            p.setUser(u);
            u.setProfile(p);
        }

        if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
            p.setFullName(req.getFullName().trim());
        } else if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được rỗng");
        }

        p.setPhone(req.getPhone() == null ? null : req.getPhone().trim());
        p.setGender(req.getGender() == null ? null : req.getGender().trim());
        p.setDob(req.getDob());
        p.setAddress(req.getAddress() == null ? null : req.getAddress().trim());
        p.setAvatar(req.getAvatar() == null ? null : req.getAvatar().trim());

        return toDetail(u);
    }

    @Transactional
    public AdminUserRowResponse toggleLock(Long id) {
        UserAccount u = userAccountRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (u.getStatus() == UserStatus.LOCKED)
            u.setStatus(UserStatus.ACTIVE);
        else
            u.setStatus(UserStatus.LOCKED);
        return toRow(u);
    }

    private AdminUserRowResponse toRow(UserAccount u) {
        AdminUserRowResponse r = new AdminUserRowResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        if (u.getProfile() != null) {
            r.setFullName(u.getProfile().getFullName());
            r.setPhone(u.getProfile().getPhone());
        }
        return r;
    }

    private AdminUserDetailResponse toDetail(UserAccount u) {
        AdminUserDetailResponse r = new AdminUserDetailResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        if (u.getProfile() != null) {
            r.setFullName(u.getProfile().getFullName());
            r.setPhone(u.getProfile().getPhone());
            r.setGender(u.getProfile().getGender());
            r.setDob(u.getProfile().getDob());
            r.setAddress(u.getProfile().getAddress());
            r.setAvatar(u.getProfile().getAvatar());
        }
        return r;
    }
}
