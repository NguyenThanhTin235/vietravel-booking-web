package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.domain.entity.auth.UserRole;
import com.vietravel.booking.domain.entity.auth.UserStatus;
import com.vietravel.booking.service.auth.AdminUserService;
import com.vietravel.booking.web.dto.auth.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController{

    private final AdminUserService adminUserService;

    public AdminUserApiController(AdminUserService adminUserService){
        this.adminUserService=adminUserService;
    }

    @GetMapping
    public List<AdminUserRowResponse> list(
            @RequestParam(value="active",required=false) Boolean active,
            @RequestParam(value="role",required=false) UserRole role,
            @RequestParam(value="status",required=false) UserStatus status,
            @RequestParam(value="q",required=false) String q
    ){
        return adminUserService.list(active,role,status,q);
    }

    @GetMapping("/{id}")
    public AdminUserDetailResponse get(@PathVariable Long id){
        return adminUserService.get(id);
    }

    @PostMapping("/staff")
    public AdminUserDetailResponse createStaff(@RequestBody AdminStaffCreateRequest req){
        return adminUserService.createStaff(req);
    }

    @PutMapping("/{id}")
    public AdminUserDetailResponse update(@PathVariable Long id,@RequestBody AdminUserUpdateRequest req){
        return adminUserService.update(id,req);
    }

    @PatchMapping("/{id}/toggle-lock")
    public AdminUserRowResponse toggleLock(@PathVariable Long id){
        return adminUserService.toggleLock(id);
    }
}
