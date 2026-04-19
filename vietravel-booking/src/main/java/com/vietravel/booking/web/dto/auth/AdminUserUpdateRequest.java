package com.vietravel.booking.web.dto.auth;

import com.vietravel.booking.domain.entity.auth.UserStatus;
import java.time.LocalDate;

public class AdminUserUpdateRequest{
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dob;
    private String address;
    private String avatar;
    private UserStatus status;

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

    public UserStatus getStatus(){return status;}
    public void setStatus(UserStatus status){this.status=status;}
}
