package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.DestinationType;
import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.domain.repository.tour.DestinationRepository;
import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import com.vietravel.booking.web.dto.tour.DestinationResponse;
import com.vietravel.booking.web.dto.tour.DestinationUpsertRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final TourCategoryRepository tourCategoryRepository;

    public DestinationService(DestinationRepository destinationRepository,
            TourCategoryRepository tourCategoryRepository) {
        this.destinationRepository = destinationRepository;
        this.tourCategoryRepository = tourCategoryRepository;
    }

    public List<DestinationResponse> list(Boolean active, Long categoryId) {
        return destinationRepository.findForAdmin(active, categoryId).stream().map(this::toRes).toList();
    }

    public DestinationResponse get(Long id) {
        Objects.requireNonNull(id, "id");
        Destination d = destinationRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến"));
        return toRes(d);
    }

    @Transactional
    public DestinationResponse create(DestinationUpsertRequest req) {
        validate(req, null);

        Destination d = new Destination();
        apply(d, req);
        destinationRepository.save(d);

        return toRes(d);
    }

    @Transactional
    public DestinationResponse update(Long id, DestinationUpsertRequest req) {
        Objects.requireNonNull(id, "id");
        Destination d = destinationRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến"));

        validate(req, id);
        apply(d, req);

        return toRes(d);
    }

    @Transactional
    public DestinationResponse toggle(Long id) {
        Objects.requireNonNull(id, "id");
        Destination d = destinationRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến"));

        d.setIsActive(!Boolean.TRUE.equals(d.getIsActive()));

        return toRes(d);
    }

    private void apply(Destination d, DestinationUpsertRequest req) {
        d.setName(req.getName().trim());

        DestinationType type = req.getType();
        if (type == null)
            type = DestinationType.CITY;
        d.setType(type);

        String slug = req.getSlug();
        if (slug == null || slug.trim().isEmpty())
            slug = toSlug(req.getName());
        d.setSlug(slug.trim());

        TourCategory cat = tourCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        d.setCategory(cat);

        d.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        d.setIsActive(req.getIsActive() == null ? true : req.getIsActive());
    }

    private void validate(DestinationUpsertRequest req, Long currentId) {
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new RuntimeException("Tên điểm đến không được rỗng");
        if (req.getCategoryId() == null)
            throw new RuntimeException("Vui lòng chọn danh mục");

        String slug = req.getSlug();
        if (slug == null || slug.trim().isEmpty())
            slug = toSlug(req.getName());
        slug = slug.trim();

        destinationRepository.findBySlug(slug).ifPresent(exist -> {
            if (currentId == null || !exist.getId().equals(currentId)) {
                throw new RuntimeException("Slug đã tồn tại");
            }
        });
    }

    private DestinationResponse toRes(Destination d) {
        DestinationResponse r = new DestinationResponse();
        r.setId(d.getId());
        r.setName(d.getName());
        r.setSlug(d.getSlug());
        r.setType(d.getType());
        r.setIsActive(d.getIsActive());
        r.setSortOrder(d.getSortOrder());
        if (d.getCategory() != null) {
            r.setCategoryId(d.getCategory().getId());
            r.setCategoryName(d.getCategory().getName());
        }
        return r;
    }

    private String toSlug(String s) {
        String x = s == null ? "" : s.trim().toLowerCase();
        x = Normalizer.normalize(x, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        x = x.replace("đ", "d");
        x = x.replaceAll("[^a-z0-9]+", "-");
        x = x.replaceAll("^-+|-+$", "");
        x = x.replaceAll("-+", "-");
        return x;
    }
}
