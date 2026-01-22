package com.vietravel.booking.service.auth;

import com.vietravel.booking.domain.entity.auth.EmailVerificationCode;
import com.vietravel.booking.domain.entity.auth.User;
import com.vietravel.booking.domain.repository.auth.EmailVerificationCodeRepository;
import com.vietravel.booking.domain.repository.auth.UserRepository;
import com.vietravel.booking.service.support.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService{

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;

    @Value("${app.verify.code-minutes:10}")
    private long codeMinutes;

    private final SecureRandom random=new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            EmailVerificationCodeRepository codeRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            JwtService jwtService
    ){
        this.userRepository=userRepository;
        this.codeRepository=codeRepository;
        this.passwordEncoder=passwordEncoder;
        this.mailService=mailService;
        this.jwtService=jwtService;
    }

    public void register(String email,String password,String fullName){
        if(userRepository.existsByEmail(email)){
            throw new RuntimeException("Email đã tồn tại");
        }

        var u=new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole("CUSTOMER");
        u.setStatus("PENDING");
        userRepository.save(u);

        var code=generate6Digits();
        var evc=new EmailVerificationCode();
        evc.setEmail(email);
        evc.setCodeHash(passwordEncoder.encode(code));
        evc.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        evc.setAttemptCount(0);
        codeRepository.save(evc);

        mailService.sendVerifyCode(email,code);
    }

    public void resendCode(String email){
        var user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("Email không tồn tại"));
        if("ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new RuntimeException("Tài khoản đã xác thực");
        }

        var code=generate6Digits();
        var evc=new EmailVerificationCode();
        evc.setEmail(email);
        evc.setCodeHash(passwordEncoder.encode(code));
        evc.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        evc.setAttemptCount(0);
        codeRepository.save(evc);

        mailService.sendVerifyCode(email,code);
    }

    public void verifyEmail(String email,String code){
        var latest=codeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(()->new RuntimeException("Chưa có mã xác thực"));

        if(latest.getUsedAt()!=null){
            throw new RuntimeException("Mã đã được sử dụng");
        }
        if(latest.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Mã đã hết hạn");
        }
        if(latest.getAttemptCount()>=5){
            throw new RuntimeException("Bạn đã nhập sai quá nhiều lần");
        }

        latest.setAttemptCount(latest.getAttemptCount()+1);
        codeRepository.save(latest);

        if(!passwordEncoder.matches(code,latest.getCodeHash())){
            throw new RuntimeException("Mã không đúng");
        }

        latest.setUsedAt(LocalDateTime.now());
        codeRepository.save(latest);

        var user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("Email không tồn tại"));
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    public String login(String email,String password){
        var user=userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("Tài khoản không tồn tại"));

        if("LOCKED".equalsIgnoreCase(user.getStatus())){
            throw new RuntimeException("Tài khoản hiện đang bị khóa");
        }

        if(!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new RuntimeException("Tài khoản chưa xác thực email");
        }

        if(!passwordEncoder.matches(password,user.getPasswordHash())){
            throw new RuntimeException("Sai mật khẩu");
        }

        return jwtService.generateAccessToken(user.getId(),user.getEmail(),user.getRole());
    }

    private String generate6Digits(){
        int n=100000+random.nextInt(900000);
        return String.valueOf(n);
    }
}
