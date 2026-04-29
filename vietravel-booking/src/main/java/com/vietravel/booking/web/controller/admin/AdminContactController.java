package com.vietravel.booking.web.controller.admin;

import com.vietravel.booking.domain.entity.support.ContactInquiryStatus;
import com.vietravel.booking.service.support.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/contacts")
public class AdminContactController {

     private final ContactService contactService;

     public AdminContactController(ContactService contactService) {
          this.contactService = contactService;
     }

     @GetMapping
     public String index(Model model,
               @RequestParam(value = "status", required = false) ContactInquiryStatus status,
               @RequestParam(value = "q", required = false) String q,
               @RequestParam(value = "id", required = false) Long id) {
          model.addAttribute("pageTitle", "Liên hệ khách hàng");
          model.addAttribute("activeMenu", "contacts");
          model.addAttribute("activeSubMenu", "");
          var inquiries = contactService.findForStaff(status, q);
          var selected = id != null ? contactService.getById(id) : (inquiries.isEmpty() ? null : inquiries.get(0));
          model.addAttribute("inquiries", inquiries);
          model.addAttribute("selectedInquiry", selected);
          model.addAttribute("selectedStatus", status != null ? status.name() : "");
          model.addAttribute("selectedQuery", q != null ? q : "");
          model.addAttribute("statusOptions", ContactInquiryStatus.values());
          return "admin/contacts/page";
     }
}
