package com.vietravel.booking.service.tour;

import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.web.dto.tour.TourCategoryResponse;
import com.vietravel.booking.web.dto.tour.TourCategoryUpsertRequest;
import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TourCategoryService {
    private final TourCategoryRepository repo;

    public TourCategoryService(TourCategoryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public TourCategoryResponse create(TourCategoryUpsertRequest req) {
        normalize(req);
        if (repo.existsBySlug(req.getSlug()))
            throw new RuntimeException("Slug đã tồn tại");
        TourCategory parent = resolveParent(req.getParentId(), null);

        TourCategory e = new TourCategory();
        e.setName(req.getName());
        e.setSlug(req.getSlug());
        e.setParent(parent);
        e.setSortOrder(req.getSortOrder());
        e.setIsActive(req.getIsActive());

        return toRes(repo.save(e));
    }

    @Transactional
    public TourCategoryResponse update(Long id, TourCategoryUpsertRequest req) {
        Objects.requireNonNull(id, "id");
        normalize(req);
        TourCategory e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        repo.findBySlug(req.getSlug()).ifPresent(exist -> {
            if (!exist.getId().equals(id))
                throw new RuntimeException("Slug đã tồn tại");
        });

        TourCategory parent = resolveParent(req.getParentId(), id);

        e.setName(req.getName());
        e.setSlug(req.getSlug());
        e.setParent(parent);
        e.setSortOrder(req.getSortOrder());
        e.setIsActive(req.getIsActive());

        return toRes(repo.save(e));
    }

    @Transactional(readOnly = true)
    public TourCategoryResponse get(Long id) {
        Objects.requireNonNull(id, "id");
        TourCategory e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        return toRes(e);
    }

    @Transactional(readOnly = true)
    public List<TourCategoryResponse> list(Boolean active) {
        List<TourCategory> items = (active != null && active)
                ? repo.findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                : repo.findAll();
        return items.stream().map(this::toRes).toList();
    }

    @Transactional
    public void delete(Long id) {
        Objects.requireNonNull(id, "id");
        if (!repo.existsById(id))
            throw new RuntimeException("Không tìm thấy danh mục");
        repo.deleteById(id);
    }

    private void normalize(TourCategoryUpsertRequest req) {
        if (req == null)
            throw new RuntimeException("Dữ liệu không hợp lệ");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new RuntimeException("Tên không được rỗng");
        req.setName(req.getName().trim());
        if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
            req.setSlug(slugify(req.getName()));
        } else {
            req.setSlug(slugify(req.getSlug()));
        }
        if (req.getSortOrder() == null)
            req.setSortOrder(0);
        if (req.getIsActive() == null)
            req.setIsActive(true);
    }

    private TourCategory resolveParent(Long parentId, Long selfId) {
        if (parentId == null)
            return null;
        if (selfId != null && parentId.equals(selfId))
            throw new RuntimeException("Danh mục cha không hợp lệ");
        TourCategory p = repo.findById(parentId).orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại"));

        if (selfId != null) {
            TourCategory cur = p;
            while (cur != null) {
                if (cur.getId().equals(selfId))
                    throw new RuntimeException("Không được chọn cha là con của chính nó");
                cur = cur.getParent();
            }
        }
        return p;
    }

    private TourCategoryResponse toRes(TourCategory e) {
        TourCategoryResponse r = new TourCategoryResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setSlug(e.getSlug());
        r.setSortOrder(e.getSortOrder());
        r.setIsActive(e.getIsActive());
        if (e.getParent() != null) {
            r.setParentId(e.getParent().getId());
            r.setParentName(e.getParent().getName());
        }
        return r;
    }

    private String slugify(String s) {
        String v = s == null ? "" : s.trim().toLowerCase();
        v = v.replace('đ', 'd');
        v = v.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        v = v.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        v = v.replaceAll("[ìíịỉĩ]", "i");
        v = v.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        v = v.replaceAll("[ùúụủũưừứựửữ]", "u");
        v = v.replaceAll("[ỳýỵỷỹ]", "y");
        v = v.replaceAll("[^a-z0-9]+", "-");
        v = v.replaceAll("(^-+)|(-+$)", "");
        v = v.replaceAll("-+", "-");
        if (v.isEmpty())
            throw new RuntimeException("Slug không hợp lệ");
        return v;
    }
}
