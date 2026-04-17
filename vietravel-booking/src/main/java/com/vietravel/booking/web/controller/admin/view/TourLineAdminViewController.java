/*package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tour-lines")
public class TourLineAdminViewController{

    @GetMapping
    public String index(Model model){
        model.addAttribute("pageTitle","Dòng tour");
        model.addAttribute("activeMenu","tourLines");
        model.addAttribute("activeSubMenu",null);
        return "admin/tour-lines/page";
    }

    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pageTitle","Thêm dòng tour");
        model.addAttribute("activeMenu","tourLines");
        model.addAttribute("activeSubMenu",null);
        return "admin/tour-lines/form-page";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam Long id,Model model){
        model.addAttribute("pageTitle","Sửa dòng tour");
        model.addAttribute("activeMenu","tourLines");
        model.addAttribute("activeSubMenu",null);
        model.addAttribute("tourLineId",id);
        return "admin/tour-lines/form-page";
    }

}*/
package com.vietravel.booking.web.controller.admin.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tour-lines")
public class TourLineAdminViewController{

    @GetMapping
    public String index(Model model){
        model.addAttribute("pageTitle","Dòng tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-lines");
        return "admin/tour-lines/page";
    }

    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pageTitle","Thêm dòng tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-lines");
        return "admin/tour-lines/form-page";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam Long id,Model model){
        model.addAttribute("pageTitle","Sửa dòng tour");
        model.addAttribute("activeMenu","tours");
        model.addAttribute("activeSubMenu","tour-lines");
        model.addAttribute("tourLineId",id);
        return "admin/tour-lines/form-page";
    }
}

