package com.vietravel.booking.domain.repository.auth;

import com.vietravel.booking.domain.entity.auth.UserAccount;
import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.auth.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAllByRole(UserRole role);

    @Query("""
                select u
                from UserAccount u
                left join fetch u.profile p
                where u.id=:id
            """)
    Optional<UserAccount> findByIdWithProfile(@Param("id") Long id);

    @Query("""
                select u
                from UserAccount u
                left join fetch u.profile p
                where (:active is null or (case when :active=true then u.status='ACTIVE' else u.status<>'ACTIVE' end)=true)
                  and (:role is null or u.role=:role)
                  and (:status is null or u.status=:status)
                  and (
                      :q is null
                      or lower(u.email) like lower(concat('%',:q,'%'))
                      or (p is not null and lower(p.fullName) like lower(concat('%',:q,'%')))
                      or (p is not null and lower(p.phone) like lower(concat('%',:q,'%')))
                  )
                order by u.createdAt desc
            """)
    List<UserAccount> findForAdmin(
            @Param("active") Boolean active,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            @Param("q") String q);
}
