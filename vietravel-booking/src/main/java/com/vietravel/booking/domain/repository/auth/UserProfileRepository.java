package com.vietravel.booking.domain.repository.auth;

import com.vietravel.booking.domain.entity.auth.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
