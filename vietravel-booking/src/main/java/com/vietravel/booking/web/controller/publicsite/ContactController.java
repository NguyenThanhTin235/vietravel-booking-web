package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.domain.entity.support.Branch;
import com.vietravel.booking.domain.entity.support.ContactInquiry;
import com.vietravel.booking.service.support.BranchService;
import com.vietravel.booking.service.support.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contact")
public class ContactController {

     private final BranchService branchService;
     private final ContactService contactService;

     public ContactController(BranchService branchService, ContactService contactService) {
          this.branchService = branchService;
          this.contactService = contactService;
     }

     @GetMapping
     public String contact(Model model,
               @RequestParam(value = "sent", required = false) String sent) {
          model.addAttribute("pageTitle", "Liên hệ");
          model.addAttribute("activeNav", "contact");

          List<Branch> branches = branchService.findAll(null, true, null);
          List<BranchRegionGroup> groups = groupByRegion(branches);
          model.addAttribute("branchGroups", groups);
          model.addAttribute("activeRegion", groups.isEmpty() ? "" : groups.get(0).getRegion());
          model.addAttribute("sentSuccess", "success".equalsIgnoreCase(sent));
          return "public/contact";
     }

     @PostMapping
     public String submit(@RequestParam("infoType") String infoType,
               @RequestParam("fullName") String fullName,
               @RequestParam("email") String email,
               @RequestParam("phone") String phone,
               @RequestParam(value = "companyName", required = false) String companyName,
               @RequestParam(value = "guestCount", required = false) Integer guestCount,
               @RequestParam(value = "address", required = false) String address,
               @RequestParam("subject") String subject,
               @RequestParam("content") String content) {
          ContactInquiry inquiry = new ContactInquiry();
          inquiry.setInfoType(infoType);
          inquiry.setFullName(fullName);
          inquiry.setEmail(email);
          inquiry.setPhone(phone);
          inquiry.setCompanyName(companyName);
          inquiry.setGuestCount(guestCount != null ? guestCount : 0);
          inquiry.setAddress(address);
          inquiry.setSubject(subject);
          inquiry.setContent(content);
          contactService.createInquiry(inquiry);
          return "redirect:/contact?sent=success";
     }

     private List<BranchRegionGroup> groupByRegion(List<Branch> branches) {
          Map<String, BranchRegionGroup> grouped = new LinkedHashMap<>();
          if (branches == null) {
               return new ArrayList<>();
          }
          for (Branch branch : branches) {
               if (branch == null || branch.getRegion() == null) {
                    continue;
               }
               String region = branch.getRegion();
               BranchRegionGroup group = grouped.computeIfAbsent(region,
                         key -> new BranchRegionGroup(key, labelForRegion(key), new ArrayList<>()));
               group.getBranches().add(branch);
          }
          return new ArrayList<>(grouped.values());
     }

     private String labelForRegion(String region) {
          if (region == null) {
               return "Khác";
          }
          switch (region) {
               case "TP_HCM":
                    return "TP.Hồ Chí Minh";
               case "MIEN_BAC":
                    return "Miền Bắc";
               case "MIEN_TRUNG":
                    return "Miền Trung";
               case "TAY_NGUYEN":
                    return "Tây Nguyên";
               case "DONG_NAM_BO":
                    return "Đông Nam Bộ";
               case "MIEN_TAY":
                    return "Miền Tây";
               case "NUOC_NGOAI":
                    return "Nước ngoài";
               default:
                    return region.replace('_', ' ');
          }
     }

     public static class BranchRegionGroup {
          private final String region;
          private final String label;
          private final List<Branch> branches;

          public BranchRegionGroup(String region, String label, List<Branch> branches) {
               this.region = region;
               this.label = label;
               this.branches = branches;
          }

          public String getRegion() {
               return region;
          }

          public String getLabel() {
               return label;
          }

          public List<Branch> getBranches() {
               return branches;
          }
     }
}
