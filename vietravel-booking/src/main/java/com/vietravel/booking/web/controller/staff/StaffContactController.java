package com.vietravel.booking.web.controller.staff;

import com.vietravel.booking.domain.entity.support.ContactInquiry;
import com.vietravel.booking.domain.entity.support.ContactInquiryStatus;
import com.vietravel.booking.service.support.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/inquiries")
public class StaffContactController {

     private final ContactService contactService;

     public StaffContactController(ContactService contactService) {
          this.contactService = contactService;
     }

     @GetMapping
     public String inquiries(Model model,
               @RequestParam(value = "status", required = false) ContactInquiryStatus status,
               @RequestParam(value = "q", required = false) String q,
               @RequestParam(value = "id", required = false) Long id) {
          var inquiries = contactService.findForStaff(status, q);
          ContactInquiry selected = id != null
                    ? contactService.getById(id)
                    : (inquiries.isEmpty() ? null : inquiries.get(0));
          model.addAttribute("inquiries", inquiries);
          model.addAttribute("selectedInquiry", selected);
          model.addAttribute("selectedStatus", status != null ? status.name() : "");
          model.addAttribute("selectedQuery", q != null ? q : "");
          model.addAttribute("statusOptions", ContactInquiryStatus.values());
          model.addAttribute("pageTitle", "Xử lý thắc mắc");
          model.addAttribute("activeMenu", "inquiries");
          model.addAttribute("activeSubMenu", "");
          return "staff/inquiries/index";
     }

     @PostMapping("/{id}/reply")
     public String reply(@PathVariable Long id,
               @RequestParam(value = "replySubject", required = false) String replySubject,
               @RequestParam(value = "replyMessage", required = false) String replyMessage) {
          boolean ok = contactService.replyToInquiry(id, replySubject, replyMessage);
          return "redirect:/staff/inquiries?id=" + id + "&toast=" + (ok ? "reply-success" : "reply-failed");
     }

     @PostMapping("/{id}/read")
     public String markRead(@PathVariable Long id) {
          contactService.markRead(id);
          return "redirect:/staff/inquiries?id=" + id + "&toast=read";
     }
}
