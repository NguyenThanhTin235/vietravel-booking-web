package com.vietravel.booking.service.auth;

import com.vietravel.booking.domain.entity.auth.EmailVerificationCode;
import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.auth.UserStatus;
import com.vietravel.booking.domain.repository.auth.EmailVerificationCodeRepository;
import com.vietravel.booking.domain.repository.auth.UserAccountRepository;
import com.vietravel.booking.domain.entity.support.NotificationType;
import com.vietravel.booking.service.support.MailService;
import com.vietravel.booking.service.support.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    @Value("${app.verify.code-minutes:10}")
    private long codeMinutes;

    private final SecureRandom random = new SecureRandom();

    public AuthService(
            UserAccountRepository userAccountRepository,
            EmailVerificationCodeRepository codeRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            JwtService jwtService,
            NotificationService notificationService) {
        this.userAccountRepository = userAccountRepository;
        this.codeRepository = codeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    public void register(String email, String password, String fullName) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        var u = new UserAccount();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(UserRole.CUSTOMER);
        u.setStatus(UserStatus.PENDING);
        u = userAccountRepository.save(u);

        var code = generate6Digits();
        var evc = new EmailVerificationCode();
        evc.setEmail(email);
        evc.setCodeHash(passwordEncoder.encode(code));
        evc.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        evc.setAttemptCount(0);
        codeRepository.save(evc);

        mailService.sendVerifyCode(Objects.requireNonNull(email, "email"), Objects.requireNonNull(code, "code"));
    }

    public void resendCode(String email) {
        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản đã xác thực");
        }

        var code = generate6Digits();
        var evc = new EmailVerificationCode();
        evc.setEmail(email);
        evc.setCodeHash(passwordEncoder.encode(code));
        evc.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        evc.setAttemptCount(0);
        codeRepository.save(evc);

        mailService.sendVerifyCode(Objects.requireNonNull(email, "email"), Objects.requireNonNull(code, "code"));
    }

    public void verifyEmail(String email, String code) {
        var latest = codeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("Chưa có mã xác thực"));

        if (latest.getUsedAt() != null) {
            throw new RuntimeException("Mã đã được sử dụng");
        }
        if (latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã đã hết hạn");
        }
        if (latest.getAttemptCount() >= 5) {
            throw new RuntimeException("Bạn đã nhập sai quá nhiều lần");
        }

        latest.setAttemptCount(latest.getAttemptCount() + 1);
        codeRepository.save(latest);

        if (!passwordEncoder.matches(code, latest.getCodeHash())) {
            throw new RuntimeException("Mã không đúng");
        }

        latest.setUsedAt(LocalDateTime.now());
        codeRepository.save(latest);

        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        user.setStatus(UserStatus.ACTIVE);
        userAccountRepository.save(user);

        notificationService.createForUser(
                user,
                "Tạo tài khoản thành công",
                "Tài khoản của bạn đã được xác thực và sẵn sàng sử dụng.",
                NotificationType.SUCCESS,
                "/profile");
    }

    public String login(String email, String password) {
        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản hiện đang bị khóa");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản chưa xác thực email");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    private String generate6Digits() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }

    public void sendForgotPasswordOtp(String email) {
        userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email này chưa được đăng ký trong hệ thống"));

        var code = generate6Digits();
        var evc = new EmailVerificationCode();
        evc.setEmail(email);
        evc.setCodeHash(passwordEncoder.encode(code));
        evc.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        evc.setAttemptCount(0);
        codeRepository.save(evc);

        mailService.sendVerifyCode(Objects.requireNonNull(email, "email"), Objects.requireNonNull(code, "code"));
    }

    public void resetPassword(String email, String code, String newPassword) {
        var latest = codeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không hợp lệ hoặc đã hết hạn"));

        if (latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        if (!passwordEncoder.matches(code, latest.getCodeHash())) {
            latest.setAttemptCount(latest.getAttemptCount() + 1);
            codeRepository.save(latest);
            throw new RuntimeException("Mã OTP không đúng");
        }

        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        latest.setUsedAt(LocalDateTime.now());
        codeRepository.save(latest);

        notificationService.createForUser(
                user,
                "Đổi mật khẩu thành công",
                "Mật khẩu của bạn đã được cập nhật. Nếu không phải bạn thực hiện, hãy liên hệ hỗ trợ ngay.",
                NotificationType.INFO,
                "/profile");
    }
}
