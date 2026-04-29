package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.TransportMode;
import com.vietravel.booking.domain.repository.tour.TransportModeRepository;
import com.vietravel.booking.web.dto.tour.TransportModeResponse;
import com.vietravel.booking.web.dto.tour.TransportModeUpsertRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TransportModeApiService {

    private final TransportModeRepository repo;

    public TransportModeApiService(TransportModeRepository repo) {
        this.repo = repo;
    }

    private void validateUpsert(TransportModeUpsertRequest req) {
        if (req.getCode() == null || req.getCode().trim().isEmpty())
            throw new RuntimeException("code không được rỗng");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new RuntimeException("name không được rỗng");
        if (req.getSortOrder() == null)
            req.setSortOrder(0);
        if (req.getIsActive() == null)
            req.setIsActive(true);
    }

    private TransportModeResponse toRes(TransportMode e) {
        TransportModeResponse r = new TransportModeResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setIsActive(e.getIsActive());
        r.setSortOrder(e.getSortOrder());
        return r;
    }

    public List<TransportModeResponse> list(Boolean onlyActive) {
        List<TransportMode> items = onlyActive != null && onlyActive
                ? repo.findByIsActiveTrueOrderBySortOrderAsc()
                : repo.findAll();
        return items.stream().map(this::toRes).toList();
    }

    public TransportModeResponse get(Long id) {
        Objects.requireNonNull(id, "id");
        TransportMode e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phương tiện"));
        return toRes(e);
    }

    public TransportModeResponse create(TransportModeUpsertRequest req) {
        validateUpsert(req);
        if (repo.existsByCode(req.getCode().trim()))
            throw new RuntimeException("code đã tồn tại");

        TransportMode e = new TransportMode();
        e.setCode(req.getCode().trim());
        e.setName(req.getName().trim());
        e.setIsActive(req.getIsActive());
        e.setSortOrder(req.getSortOrder());

        return toRes(repo.save(e));
    }

    public TransportModeResponse update(Long id, TransportModeUpsertRequest req) {
        Objects.requireNonNull(id, "id");
        validateUpsert(req);
        TransportMode e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phương tiện"));
        if (repo.existsByCodeAndIdNot(req.getCode().trim(), id))
            throw new RuntimeException("code đã tồn tại");

        e.setCode(req.getCode().trim());
        e.setName(req.getName().trim());
        e.setIsActive(req.getIsActive());
        e.setSortOrder(req.getSortOrder());

        return toRes(repo.save(e));
    }

    public void delete(Long id) {
        Objects.requireNonNull(id, "id");
        if (!repo.existsById(id))
            throw new RuntimeException("Không tìm thấy phương tiện");
        repo.deleteById(id);
    }
}
