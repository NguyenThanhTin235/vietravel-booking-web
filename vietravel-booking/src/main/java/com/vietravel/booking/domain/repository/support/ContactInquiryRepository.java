package com.vietravel.booking.domain.repository.support;

import com.vietravel.booking.domain.entity.support.ContactInquiry;
import com.vietravel.booking.domain.entity.support.ContactInquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {
     @Query("""
               		select c
               		from ContactInquiry c
               		where (:status is null or c.status = :status)
               			and (
               						:q is null
               						or lower(c.fullName) like lower(concat('%', :q, '%'))
               						or lower(c.email) like lower(concat('%', :q, '%'))
               						or lower(c.phone) like lower(concat('%', :q, '%'))
               						or lower(c.subject) like lower(concat('%', :q, '%'))
               			)
               		order by c.createdAt desc
               """)
     List<ContactInquiry> findForStaff(
               @Param("status") ContactInquiryStatus status,
               @Param("q") String q);
}
