package com.vietravel.booking.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class UserAccount{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,unique=true,length=255)
    private String email;

    @Column(name="password_hash",nullable=false,length=255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private UserStatus status;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at",nullable=false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy="user",fetch=FetchType.LAZY,cascade=CascadeType.ALL,orphanRemoval=true)
    private UserProfile profile;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}

    public String getPasswordHash(){return passwordHash;}
    public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}

    public UserRole getRole(){return role;}
    public void setRole(UserRole role){this.role=role;}

    public UserStatus getStatus(){return status;}
    public void setStatus(UserStatus status){this.status=status;}

    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}

    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}

    public UserProfile getProfile(){return profile;}
    public void setProfile(UserProfile profile){this.profile=profile;}

    @PrePersist
    public void prePersist(){
        LocalDateTime now=LocalDateTime.now();
        if(createdAt==null) createdAt=now;
        if(updatedAt==null) updatedAt=now;
        if(status==null) status=UserStatus.ACTIVE;
        if(role==null) role=UserRole.CUSTOMER;
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt=LocalDateTime.now();
    }
}
