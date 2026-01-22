package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.auth.AuthService;
import com.vietravel.booking.service.auth.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController{

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthPageController(AuthService authService,JwtService jwtService){
        this.authService=authService;
        this.jwtService=jwtService;
    }

    @GetMapping("/auth/login")
    public String loginPage(){
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage(){
        return "auth/register";
    }

    @GetMapping("/auth/verify")
    public String verifyPage(){
        return "auth/verify";
    }

    @PostMapping("/auth/register-form")
    public String doRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes ra
    ){
        try{
            if(!password.equals(confirmPassword)){
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }

            authService.register(email,password,fullName);

            ra.addFlashAttribute("success","Đăng ký thành công. Vui lòng nhập mã OTP đã gửi qua email.");
            return "redirect:/auth/verify?email="+email;
        }catch(RuntimeException e){
            ra.addFlashAttribute("error",e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @PostMapping("/auth/verify-form")
    public String doVerify(
            @RequestParam String email,
            @RequestParam String code,
            RedirectAttributes ra
    ){
        try{
            authService.verifyEmail(email,code);
            ra.addFlashAttribute("success","Xác thực email thành công. Bạn có thể đăng nhập.");
            return "redirect:/auth/login";
        }catch(RuntimeException e){
            ra.addFlashAttribute("error",e.getMessage());
            return "redirect:/auth/verify?email="+email;
        }
    }

    @GetMapping("/auth/resend-code")
    public String resendCode(@RequestParam String email,RedirectAttributes ra){
        try{
            authService.resendCode(email);
            ra.addFlashAttribute("success","Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        }catch(RuntimeException e){
            ra.addFlashAttribute("error",e.getMessage());
        }
        return "redirect:/auth/verify?email="+email;
    }

    @PostMapping("/auth/login-form")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes ra
    ){
        try{
            var token=authService.login(email,password);
            response.addHeader(
                    "Set-Cookie",
                    "accessToken="+token+"; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400"
            );
            return "redirect:/auth/role-redirect";
        }catch(RuntimeException e){
            ra.addFlashAttribute("error",e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth/role-redirect")
    public String roleRedirect(@CookieValue(name="accessToken",required=false) String token){
        if(token==null||token.isBlank()){
            return "redirect:/auth/login";
        }

        var claims=jwtService.parseClaims(token);
        var role=String.valueOf(claims.get("role"));

        if("ADMIN".equalsIgnoreCase(role)) return "redirect:/admin";
        if("STAFF".equalsIgnoreCase(role)) return "redirect:/staff";
        return "redirect:/";
    }
}
