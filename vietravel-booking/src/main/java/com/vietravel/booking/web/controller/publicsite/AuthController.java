package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.auth.AuthService;
import com.vietravel.booking.web.dto.auth.LoginRequest;
import com.vietravel.booking.web.dto.auth.RegisterRequest;
import com.vietravel.booking.web.dto.auth.VerifyRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController{

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
        authService.register(req.getEmail(),req.getPassword(),req.getFullName());
        return ResponseEntity.ok(Map.of("message","Vui lòng kiểm tra email để lấy mã xác thực."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest req){
        authService.verifyEmail(req.getEmail(),req.getCode());
        return ResponseEntity.ok(Map.of("message","Xác thực email thành công. Bạn có thể đăng nhập."));
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody Map<String,String> body){
        var email=body.get("email");
        authService.resendCode(email);
        return ResponseEntity.ok(Map.of("message","Đã gửi lại mã xác thực. Vui lòng kiểm tra email."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req){
        var token=authService.login(req.getEmail(),req.getPassword());
        return ResponseEntity.ok(Map.of("accessToken",token,"tokenType","Bearer"));
    }
}
