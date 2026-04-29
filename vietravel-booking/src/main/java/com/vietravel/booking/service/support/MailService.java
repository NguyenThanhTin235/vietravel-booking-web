package com.vietravel.booking.service.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @SuppressWarnings("null")
    public void sendVerifyCode(@NonNull String toEmail, @NonNull String otp) {
        Objects.requireNonNull(toEmail, "toEmail");
        Objects.requireNonNull(otp, "otp");
        String subject = "[Vietravel Booking] Mã OTP xác thực tài khoản";

        String content = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #0d6efd;">Xin chào,</h2>

                    <p>Cảm ơn bạn đã đăng ký tài khoản tại
                        <strong>Vietravel Booking</strong>.
                    </p>

                    <p>Mã OTP xác thực email của bạn là:</p>

                    <h1 style="
                        background: #f2f2f2;
                        padding: 12px 20px;
                        display: inline-block;
                        border-radius: 8px;
                        color: #0d6efd;
                        letter-spacing: 4px;
                        font-weight: bold;
                    ">
                        %s
                    </h1>

                    <p>Mã OTP này sẽ hết hạn sau <strong>5 phút</strong>.</p>

                    <p style="margin-top: 24px;">
                        Trân trọng,<br>
                        <strong>Đội ngũ Vietravel Booking</strong>
                    </p>

                    <hr style="margin-top:20px;">

                    <p style="font-size:13px; color:#888;">
                        Email này được gửi tự động, vui lòng không trả lời.
                    </p>
                </div>
                """.formatted(otp);

        sendHtmlMail(toEmail, subject, content, "Vietravel Booking");
    }

    private void sendHtmlMail(@NonNull String to,
            @NonNull String subject,
            @NonNull String htmlContent,
            @NonNull String fromName) {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(htmlContent, "htmlContent");
        Objects.requireNonNull(fromName, "fromName");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("no-reply@vietravel-booking.com", fromName);

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Không thể gửi email xác thực", e);
        }
    }
}
