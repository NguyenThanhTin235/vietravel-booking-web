package com.vietravel.booking.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="user_profile")
public class UserProfile{

    @Id
    @Column(name="user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserAccount user;

    @Column(name="full_name",nullable=false,length=200)
    private String fullName;

    @Column(length=30)
    private String phone;

    @Column(length=10)
    private String gender;

    private LocalDate dob;

    @Column(length=300)
    private String address;

    @Column(length=500)
    private String avatar;

    @Column(name="updated_at",nullable=false)
    private LocalDateTime updatedAt;

    public Long getUserId(){return userId;}
    public void setUserId(Long userId){this.userId=userId;}

    public UserAccount getUser(){return user;}
    public void setUser(UserAccount user){this.user=user;}

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

    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}

    @PrePersist
    public void prePersist(){
        if(updatedAt==null) updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt=LocalDateTime.now();
    }
}
