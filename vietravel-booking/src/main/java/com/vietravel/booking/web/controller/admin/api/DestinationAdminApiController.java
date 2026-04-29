package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.web.dto.tour.DestinationResponse;
import com.vietravel.booking.web.dto.tour.DestinationUpsertRequest;
import com.vietravel.booking.service.tour.DestinationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/destinations")
public class DestinationAdminApiController{

    private final DestinationService destinationService;

    public DestinationAdminApiController(DestinationService destinationService){
        this.destinationService=destinationService;
    }

    @GetMapping
    public List<DestinationResponse> list(
            @RequestParam(value="active",required=false) Boolean active,
            @RequestParam(value="categoryId",required=false) Long categoryId
    ){
        return destinationService.list(active,categoryId);
    }

    @GetMapping("/{id}")
    public DestinationResponse get(@PathVariable Long id){
        return destinationService.get(id);
    }

    @PostMapping
    public DestinationResponse create(@RequestBody DestinationUpsertRequest req){
        return destinationService.create(req);
    }

    @PutMapping("/{id}")
    public DestinationResponse update(@PathVariable Long id,@RequestBody DestinationUpsertRequest req){
        return destinationService.update(id,req);
    }

    @PatchMapping("/{id}/toggle")
    public DestinationResponse toggle(@PathVariable Long id){
        return destinationService.toggle(id);
    }
}
