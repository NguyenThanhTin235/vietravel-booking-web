package com.vietravel.booking.web.dto.auth;

import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.auth.UserStatus;
import java.time.LocalDateTime;

public class AdminUserRowResponse{
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}

    public String getFullName(){return fullName;}
    public void setFullName(String fullName){this.fullName=fullName;}

    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone=phone;}

    public UserRole getRole(){return role;}
    public void setRole(UserRole role){this.role=role;}

    public UserStatus getStatus(){return status;}
    public void setStatus(UserStatus status){this.status=status;}

    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
