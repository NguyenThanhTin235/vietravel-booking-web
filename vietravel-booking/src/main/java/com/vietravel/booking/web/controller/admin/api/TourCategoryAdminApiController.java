package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.web.dto.tour.TourCategoryResponse;
import com.vietravel.booking.web.dto.tour.TourCategoryUpsertRequest;
import com.vietravel.booking.service.tour.TourCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tour-categories")
public class TourCategoryAdminApiController{
    private final TourCategoryService service;

    public TourCategoryAdminApiController(TourCategoryService service){
        this.service=service;
    }

    @GetMapping
    public List<TourCategoryResponse> list(@RequestParam(value="active",required=false) Boolean active){
        return service.list(active);
    }

    @GetMapping("/{id}")
    public TourCategoryResponse get(@PathVariable Long id){
        return service.get(id);
    }

    @PostMapping
    public TourCategoryResponse create(@RequestBody TourCategoryUpsertRequest req){
        return service.create(req);
    }

    @PutMapping("/{id}")
    public TourCategoryResponse update(@PathVariable Long id,@RequestBody TourCategoryUpsertRequest req){
        return service.update(id,req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
}
