package com.vietravel.booking.domain.entity.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="email_verification_codes")
public class EmailVerificationCode{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,length=255)
    private String email;

    @Column(name="code_hash",nullable=false,length=255)
    private String codeHash;

    @Column(name="expires_at",nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="used_at")
    private LocalDateTime usedAt;

    @Column(name="attempt_count",nullable=false)
    private int attemptCount;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        createdAt=LocalDateTime.now();
    }
}
