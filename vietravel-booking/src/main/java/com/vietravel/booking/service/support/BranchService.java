package com.vietravel.booking.service.support;

import com.vietravel.booking.domain.entity.support.Branch;
import com.vietravel.booking.domain.repository.support.BranchRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

     private final BranchRepository branchRepository;

     public BranchService(BranchRepository branchRepository) {
          this.branchRepository = branchRepository;
     }

     @Transactional(readOnly = true)
     public List<Branch> findAll(String region, Boolean active, String keyword) {
          List<Branch> branches = branchRepository.findAll(Sort.by(
                    Sort.Order.asc("sortOrder"),
                    Sort.Order.asc("id")));

          String regionKey = normalize(region);
          String searchKey = normalize(keyword);

          if (!regionKey.isEmpty()) {
               branches = branches.stream()
                         .filter(b -> contains(b.getRegion(), regionKey))
                         .collect(Collectors.toList());
          }

          if (active != null) {
               branches = branches.stream()
                         .filter(b -> active.equals(b.getIsActive()))
                         .collect(Collectors.toList());
          }

          if (!searchKey.isEmpty()) {
               branches = branches.stream()
                         .filter(b -> matchesKeyword(b, searchKey))
                         .collect(Collectors.toList());
          }

          return branches;
     }

     @Transactional(readOnly = true)
     public Branch getById(Long id) {
          return id == null ? null : branchRepository.findById(id).orElse(null);
     }

     @Transactional
     public Branch create(Branch branch) {
          if (branch == null) {
               return null;
          }
          normalizeBranch(branch);
          return branchRepository.save(branch);
     }

     @Transactional
     public Branch update(Long id, Branch input) {
          if (id == null || input == null) {
               return null;
          }
          Branch existing = branchRepository.findById(id).orElse(null);
          if (existing == null) {
               return null;
          }
          existing.setRegion(normalizeText(input.getRegion()));
          existing.setName(normalizeText(input.getName()));
          existing.setAddress(normalizeText(input.getAddress()));
          existing.setHotline(normalizeNullable(input.getHotline()));
          existing.setEmail(normalizeNullable(input.getEmail()));
          existing.setFax(normalizeNullable(input.getFax()));
          existing.setIsActive(input.getIsActive() != null ? input.getIsActive() : Boolean.TRUE);
          existing.setSortOrder(input.getSortOrder() != null ? input.getSortOrder() : 0);
          return branchRepository.save(existing);
     }

     @Transactional
     public boolean toggleActive(Long id) {
          Branch branch = branchRepository.findById(id).orElse(null);
          if (branch == null) {
               return false;
          }
          branch.setIsActive(branch.getIsActive() == null || !branch.getIsActive());
          branchRepository.save(branch);
          return true;
     }

     @Transactional
     public boolean delete(Long id) {
          if (id == null || !branchRepository.existsById(id)) {
               return false;
          }
          branchRepository.deleteById(id);
          return true;
     }

     private void normalizeBranch(Branch branch) {
          branch.setRegion(normalizeText(branch.getRegion()));
          branch.setName(normalizeText(branch.getName()));
          branch.setAddress(normalizeText(branch.getAddress()));
          branch.setHotline(normalizeNullable(branch.getHotline()));
          branch.setEmail(normalizeNullable(branch.getEmail()));
          branch.setFax(normalizeNullable(branch.getFax()));
          if (branch.getIsActive() == null) {
               branch.setIsActive(Boolean.TRUE);
          }
          if (branch.getSortOrder() == null) {
               branch.setSortOrder(0);
          }
     }

     private boolean matchesKeyword(Branch branch, String keyword) {
          if (branch == null) {
               return false;
          }
          return contains(branch.getName(), keyword)
                    || contains(branch.getAddress(), keyword)
                    || contains(branch.getHotline(), keyword)
                    || contains(branch.getEmail(), keyword)
                    || contains(branch.getFax(), keyword);
     }

     private String normalize(String value) {
          return value == null ? "" : value.trim().toLowerCase();
     }

     private String normalizeText(String value) {
          return value == null ? "" : value.trim();
     }

     private String normalizeNullable(String value) {
          if (value == null) {
               return null;
          }
          String trimmed = value.trim();
          return trimmed.isEmpty() ? null : trimmed;
     }

     private boolean contains(String value, String keyword) {
          if (value == null) {
               return false;
          }
          return value.toLowerCase().contains(keyword);
     }
}
