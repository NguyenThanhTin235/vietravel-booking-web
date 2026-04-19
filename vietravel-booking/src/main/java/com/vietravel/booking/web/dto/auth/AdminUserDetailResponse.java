package com.vietravel.booking.web.dto.auth;

import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.auth.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminUserDetailResponse{
    private Long id;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dob;
    private String address;
    private String avatar;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}

    public UserRole getRole(){return role;}
    public void setRole(UserRole role){this.role=role;}

    public UserStatus getStatus(){return status;}
    public void setStatus(UserStatus status){this.status=status;}

    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}

    public String getFullName(){return fullName;}
    public void setFullName(String fullName){this.fullName=fullName;}

    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone=phone;}

    public String getGender(){return gender;}
    public void setGender(String gender){this.gender=gender;}

    public LocalDate getDob(){return dob;}
    public void setDob(LocalDate dob){this.dob=dob;}

    public String getAddress(){return address;}
    public void setAddress(String address){this.address=address;}

    public String getAvatar(){return avatar;}
    public void setAvatar(String avatar){this.avatar=avatar;}
}
