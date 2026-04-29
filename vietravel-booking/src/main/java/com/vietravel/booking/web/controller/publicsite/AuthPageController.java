package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.auth.AuthService;
import com.vietravel.booking.service.auth.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthPageController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/auth/verify")
    public String verifyPage() {
        return "auth/verify";
    }

    @PostMapping("/auth/register-form")
    public String doRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }

            authService.register(email, password, fullName);

            ra.addFlashAttribute("success", "Đăng ký thành công. Vui lòng nhập mã OTP đã gửi qua email.");
            return "redirect:/auth/verify?email=" + email;
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @PostMapping("/auth/verify-form")
    public String doVerify(
            @RequestParam String email,
            @RequestParam String code,
            RedirectAttributes ra) {
        try {
            authService.verifyEmail(email, code);
            ra.addFlashAttribute("success", "Xác thực email thành công. Bạn có thể đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/verify?email=" + email;
        }
    }

    @GetMapping("/auth/resend-code")
    public String resendCode(@RequestParam String email, RedirectAttributes ra) {
        try {
            authService.resendCode(email);
            ra.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/auth/verify?email=" + email;
    }

    @PostMapping("/auth/login-form")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes ra) {
        try {
            var token = authService.login(email, password);
            response.addHeader(
                    "Set-Cookie",
                    "accessToken=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400");
            return "redirect:/auth/role-redirect";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addHeader(
                "Set-Cookie",
                "accessToken=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
        return "redirect:/auth/login";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletResponse response) {
        return logout(response);
    }

    @GetMapping("/auth/role-redirect")
    public String roleRedirect(@CookieValue(name = "accessToken", required = false) String token) {
        if (token == null || token.isBlank()) {
            return "redirect:/auth/login";
        }

        var claims = jwtService.parseClaims(token);
        var role = String.valueOf(claims.get("role"));
        var normalizedRole = role == null ? "" : role.replaceFirst("^ROLE_", "");

        if ("ADMIN".equalsIgnoreCase(normalizedRole))
            return "redirect:/admin";
        if ("STAFF".equalsIgnoreCase(normalizedRole))
            return "redirect:/staff";
        return "redirect:/";
    }

    @GetMapping("/auth/forgot")
    public String showForgotPage() {
        return "auth/forgot";
    }

    @PostMapping("/auth/forgot-form")
    public String processForgot(@RequestParam String email, RedirectAttributes ra) {
        try {
            authService.sendForgotPasswordOtp(email);
            return "redirect:/auth/verify-forgot?email=" + email;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot";
        }
    }

    @GetMapping("/auth/verify-forgot")
    public String showVerifyForgot(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify-forgot";
    }

    @GetMapping("/auth/reset-password")
    public String showResetPassword(@RequestParam String email, @RequestParam String code, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("code", code);
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password-form")
    public String processResetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }
            authService.resetPassword(email, code, password);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?email=" + email + "&code=" + code;
        }
    }
}
