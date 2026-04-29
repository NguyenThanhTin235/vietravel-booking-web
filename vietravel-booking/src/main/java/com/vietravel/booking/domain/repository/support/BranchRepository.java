package com.vietravel.booking.domain.repository.support;

import com.vietravel.booking.domain.entity.support.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}
