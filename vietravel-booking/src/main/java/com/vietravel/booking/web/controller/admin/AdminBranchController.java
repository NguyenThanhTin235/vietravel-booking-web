package com.vietravel.booking.web.controller.admin;

import com.vietravel.booking.domain.entity.support.Branch;
import com.vietravel.booking.service.support.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

     private final BranchService branchService;

     public AdminBranchController(BranchService branchService) {
          this.branchService = branchService;
     }

     @GetMapping
     public String index(Model model,
               @RequestParam(value = "region", required = false) String region,
               @RequestParam(value = "active", required = false) Boolean active,
               @RequestParam(value = "q", required = false) String q,
               @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
          model.addAttribute("pageTitle", "Quản lý chi nhánh");
          model.addAttribute("activeMenu", "branches");
          model.addAttribute("activeSubMenu", "");

          List<Branch> branches = branchService.findAll(region, active, q);

          int pageSize = 10;
          int totalItems = branches.size();
          int currentPage = Math.max(1, page);
          int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
          int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
          int toIndex = Math.min(fromIndex + pageSize, totalItems);

          model.addAttribute("branches", branches.subList(fromIndex, toIndex));
          model.addAttribute("currentPage", currentPage);
          model.addAttribute("totalPages", Math.max(totalPages, 1));
          model.addAttribute("selectedRegion", region != null ? region : "");
          model.addAttribute("selectedActive", active != null ? active.toString() : "");
          model.addAttribute("selectedQuery", q != null ? q : "");
          return "admin/branches/page";
     }

     @GetMapping("/create")
     public String createForm(Model model) {
          model.addAttribute("pageTitle", "Thêm chi nhánh");
          model.addAttribute("activeMenu", "branches");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("branch", new Branch());
          model.addAttribute("mode", "create");
          return "admin/branches/form-page";
     }

     @GetMapping("/{id}/edit")
     public String editForm(@PathVariable Long id, Model model) {
          model.addAttribute("pageTitle", "Cập nhật chi nhánh");
          model.addAttribute("activeMenu", "branches");
          model.addAttribute("activeSubMenu", "");
          model.addAttribute("branch", branchService.getById(id));
          model.addAttribute("mode", "edit");
          return "admin/branches/form-page";
     }

     @PostMapping
     public String create(@RequestParam String region,
               @RequestParam String name,
               @RequestParam String address,
               @RequestParam(value = "hotline", required = false) String hotline,
               @RequestParam(value = "email", required = false) String email,
               @RequestParam(value = "fax", required = false) String fax,
               @RequestParam(value = "isActive", required = false) Boolean isActive,
               @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
          Branch branch = new Branch();
          branch.setRegion(region);
          branch.setName(name);
          branch.setAddress(address);
          branch.setHotline(hotline);
          branch.setEmail(email);
          branch.setFax(fax);
          branch.setIsActive(isActive != null ? isActive : Boolean.TRUE);
          branch.setSortOrder(sortOrder != null ? sortOrder : 0);
          branchService.create(branch);
          return "redirect:/admin/branches?toast=create-success";
     }

     @PostMapping("/{id}")
     public String update(@PathVariable Long id,
               @RequestParam String region,
               @RequestParam String name,
               @RequestParam String address,
               @RequestParam(value = "hotline", required = false) String hotline,
               @RequestParam(value = "email", required = false) String email,
               @RequestParam(value = "fax", required = false) String fax,
               @RequestParam(value = "isActive", required = false) Boolean isActive,
               @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
          Branch branch = new Branch();
          branch.setRegion(region);
          branch.setName(name);
          branch.setAddress(address);
          branch.setHotline(hotline);
          branch.setEmail(email);
          branch.setFax(fax);
          branch.setIsActive(isActive != null ? isActive : Boolean.TRUE);
          branch.setSortOrder(sortOrder != null ? sortOrder : 0);
          branchService.update(id, branch);
          return "redirect:/admin/branches?toast=update-success";
     }

     @PostMapping("/{id}/toggle")
     public String toggle(@PathVariable Long id) {
          branchService.toggleActive(id);
          return "redirect:/admin/branches?toast=toggle-success";
     }

     @PostMapping("/{id}/delete")
     public String delete(@PathVariable Long id) {
          branchService.delete(id);
          return "redirect:/admin/branches?toast=delete-success";
     }
}
