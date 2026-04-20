package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.TourLine;
import com.vietravel.booking.domain.repository.tour.TourLineRepository;
import com.vietravel.booking.web.dto.tour.TourLineResponse;
import com.vietravel.booking.web.dto.tour.TourLineUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TourLineApiService {

    private final TourLineRepository repo;

    public TourLineApiService(TourLineRepository repo) {
        this.repo = repo;
    }

    private String normCode(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    private void validateUpsert(TourLineUpsertRequest req) {
        if (req == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
        if (req.getCode() == null || req.getCode().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code không được rỗng");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name không được rỗng");
        if (req.getMinPrice() == null || req.getMaxPrice() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice/maxPrice không được rỗng");
        if (req.getMinPrice().compareTo(BigDecimal.ZERO) < 0 || req.getMaxPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "giá không hợp lệ");
        if (req.getMinPrice().compareTo(req.getMaxPrice()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice phải <= maxPrice");
        if (req.getSortOrder() == null)
            req.setSortOrder(0);
        if (req.getIsActive() == null)
            req.setIsActive(true);

        req.setCode(normCode(req.getCode()));
        req.setName(req.getName().trim());
    }

    private TourLineResponse toRes(TourLine e) {
        TourLineResponse r = new TourLineResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setMinPrice(e.getMinPrice());
        r.setMaxPrice(e.getMaxPrice());
        r.setIsActive(e.getIsActive());
        r.setSortOrder(e.getSortOrder());
        return r;
    }

    public List<TourLineResponse> list(Boolean onlyActive) {
        List<TourLine> items = onlyActive != null && onlyActive ? repo.findByIsActiveTrueOrderBySortOrderAsc()
                : repo.findAll();
        return items.stream().map(this::toRes).toList();
    }

    public TourLineResponse get(Long id) {
        Objects.requireNonNull(id, "id");
        TourLine e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour line"));
        return toRes(e);
    }

    public TourLineResponse create(TourLineUpsertRequest req) {
        validateUpsert(req);

        if (repo.existsByCode(req.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code đã tồn tại");
        }

        TourLine e = new TourLine();
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setMinPrice(req.getMinPrice());
        e.setMaxPrice(req.getMaxPrice());
        e.setIsActive(req.getIsActive());
        e.setSortOrder(req.getSortOrder());

        return toRes(repo.save(e));
    }

    public TourLineResponse update(Long id, TourLineUpsertRequest req) {
        Objects.requireNonNull(id, "id");
        validateUpsert(req);

        TourLine e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour line"));

        if (repo.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code đã tồn tại");
        }

        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setMinPrice(req.getMinPrice());
        e.setMaxPrice(req.getMaxPrice());
        e.setIsActive(req.getIsActive());
        e.setSortOrder(req.getSortOrder());

        return toRes(repo.save(e));
    }

    public void delete(Long id) {
        Objects.requireNonNull(id, "id");
        if (!repo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tour line");
        repo.deleteById(id);
    }

    public TourLineResponse resolveByPrice(BigDecimal price) {
        if (price == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price không được rỗng");
        return repo.findFirstByIsActiveTrueAndMinPriceLessThanEqualAndMaxPriceGreaterThanEqual(price, price)
                .map(this::toRes)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không có tour line phù hợp với giá"));
    }
}
