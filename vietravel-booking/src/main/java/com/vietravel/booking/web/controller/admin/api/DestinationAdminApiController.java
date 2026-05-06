package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.web.dto.tour.DestinationResponse;
import com.vietravel.booking.web.dto.tour.DestinationUpsertRequest;
import com.vietravel.booking.service.tour.DestinationService;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/admin/destinations")
public class DestinationAdminApiController {

    private static final Logger log = LoggerFactory.getLogger(DestinationAdminApiController.class);

    private final DestinationService destinationService;

    public DestinationAdminApiController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public List<DestinationResponse> list(
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        logCurrentUser();
        return destinationService.list(active, categoryId);
    }

    @GetMapping("/{id}")
    public DestinationResponse get(@PathVariable Long id) {
        logCurrentUser();
        return destinationService.get(id);
    }

    @PostMapping
    public DestinationResponse create(@RequestBody DestinationUpsertRequest req) {
        logCurrentUser();
        return destinationService.create(req);
    }

    @PutMapping("/{id}")
    public DestinationResponse update(@PathVariable Long id, @RequestBody DestinationUpsertRequest req) {
        logCurrentUser();
        return destinationService.update(id, req);
    }

    @PatchMapping("/{id}/toggle")
    public DestinationResponse toggle(@PathVariable Long id) {
        logCurrentUser();
        return destinationService.toggle(id);
    }

    private void logCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("[SECURITY] No authentication found");
            return;
        }
        log.info("[SECURITY] Principal: {} | Authorities: {} | Authenticated: {}", auth.getName(),
                auth.getAuthorities(), auth.isAuthenticated());
    }
}
