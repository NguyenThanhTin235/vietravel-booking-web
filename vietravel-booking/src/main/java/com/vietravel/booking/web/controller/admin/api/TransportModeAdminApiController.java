package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.tour.TransportModeApiService;
import com.vietravel.booking.web.dto.tour.TransportModeResponse;
import com.vietravel.booking.web.dto.tour.TransportModeUpsertRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transport-modes")
public class TransportModeAdminApiController{

    private final TransportModeApiService service;

    public TransportModeAdminApiController(TransportModeApiService service){
        this.service=service;
    }

    @GetMapping
    public List<TransportModeResponse> list(@RequestParam(required=false) Boolean active){
        return service.list(active);
    }

    @GetMapping("/{id}")
    public TransportModeResponse get(@PathVariable Long id){
        return service.get(id);
    }

    @PostMapping
    public TransportModeResponse create(@RequestBody TransportModeUpsertRequest req){
        return service.create(req);
    }

    @PutMapping("/{id}")
    public TransportModeResponse update(@PathVariable Long id,@RequestBody TransportModeUpsertRequest req){
        return service.update(id,req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
}
