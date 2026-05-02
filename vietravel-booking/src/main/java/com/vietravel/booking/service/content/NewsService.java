package com.vietravel.booking.service.content;

import com.vietravel.booking.domain.entity.content.News;
import com.vietravel.booking.domain.entity.content.NewsStatus;
import com.vietravel.booking.domain.repository.content.NewsRepository;
import com.vietravel.booking.web.dto.content.NewsPublicDetailView;
import com.vietravel.booking.web.dto.content.NewsPublicListItem;
import com.vietravel.booking.web.dto.content.NewsResponse;
import com.vietravel.booking.web.dto.content.NewsUpsertRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class NewsService {

     private final NewsRepository newsRepository;

     public NewsService(NewsRepository newsRepository) {
          this.newsRepository = newsRepository;
     }

     @Transactional(readOnly = true)
     public List<NewsResponse> listAll() {
          return newsRepository.findAllOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
     }

     @Transactional(readOnly = true)
     public NewsResponse get(Long id) {
          Objects.requireNonNull(id, "id");
          News news = newsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));
          return toResponse(news);
     }

     @Transactional
     public NewsResponse create(NewsUpsertRequest req) {
          normalize(req);
          if (newsRepository.existsBySlug(req.getSlug())) {
               throw new RuntimeException("Slug đã tồn tại");
          }
          News news = new News();
          applyToEntity(news, req);
          return toResponse(newsRepository.save(news));
     }

     @Transactional
     public NewsResponse update(Long id, NewsUpsertRequest req) {
          Objects.requireNonNull(id, "id");
          normalize(req);
          News news = newsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));

          newsRepository.findBySlug(req.getSlug()).ifPresent(exist -> {
               if (!exist.getId().equals(id)) {
                    throw new RuntimeException("Slug đã tồn tại");
               }
          });

          applyToEntity(news, req);
          return toResponse(newsRepository.save(news));
     }

     @Transactional
     public void delete(Long id) {
          Objects.requireNonNull(id, "id");
          if (!newsRepository.existsById(id)) {
               throw new RuntimeException("Không tìm thấy tin tức");
          }
          newsRepository.deleteById(id);
     }

     @Transactional(readOnly = true)
     public List<NewsPublicListItem> listPublic() {
          return newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.PUBLISHED).stream()
                    .sorted(Comparator.comparing(News::getCreatedAt).reversed())
                    .map(this::toPublicListItem)
                    .toList();
     }

     @Transactional
     public NewsPublicDetailView getPublicDetail(String slug) {
          if (slug == null || slug.isBlank()) {
               throw new RuntimeException("Không tìm thấy tin tức");
          }
          News news = newsRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));
          if (news.getStatus() != NewsStatus.PUBLISHED) {
               throw new RuntimeException("Tin tức chưa được phát hành");
          }
          news.setViewCount(safeInt(news.getViewCount()) + 1);
          newsRepository.save(news);
          return toPublicDetail(news);
     }

     private void normalize(NewsUpsertRequest req) {
          if (req == null) {
               throw new RuntimeException("Dữ liệu không hợp lệ");
          }
          if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
               throw new RuntimeException("Tiêu đề không được rỗng");
          }
          req.setTitle(req.getTitle().trim());
          if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
               req.setSlug(slugify(req.getTitle()));
          } else {
               req.setSlug(slugify(req.getSlug()));
          }
          if (req.getContentHtml() == null || req.getContentHtml().trim().isEmpty()) {
               throw new RuntimeException("Nội dung không được rỗng");
          }
          req.setContentHtml(req.getContentHtml().trim());
          if (req.getIsFeatured() == null) {
               req.setIsFeatured(false);
          }
          if (req.getStatus() == null) {
               req.setStatus(NewsStatus.PUBLISHED);
          }
     }

     private void applyToEntity(News news, NewsUpsertRequest req) {
          news.setTitle(req.getTitle());
          news.setSlug(req.getSlug());
          news.setThumbnail(req.getThumbnail());
          news.setSummary(req.getSummary());
          news.setContentHtml(req.getContentHtml());
          news.setIsFeatured(req.getIsFeatured());
          news.setStatus(req.getStatus());
     }

     private NewsResponse toResponse(News news) {
          NewsResponse res = new NewsResponse();
          res.setId(news.getId());
          res.setTitle(news.getTitle());
          res.setSlug(news.getSlug());
          res.setThumbnail(news.getThumbnail());
          res.setSummary(news.getSummary());
          res.setContentHtml(news.getContentHtml());
          res.setViewCount(news.getViewCount());
          res.setIsFeatured(news.getIsFeatured());
          res.setStatus(news.getStatus());
          res.setCreatedAt(news.getCreatedAt());
          res.setUpdatedAt(news.getUpdatedAt());
          return res;
     }

     private NewsPublicListItem toPublicListItem(News news) {
          NewsPublicListItem item = new NewsPublicListItem();
          item.setId(news.getId());
          item.setTitle(news.getTitle());
          item.setSlug(news.getSlug());
          item.setThumbnail(news.getThumbnail());
          item.setSummary(news.getSummary());
          item.setCreatedAt(news.getCreatedAt());
          item.setViewCount(news.getViewCount());
          return item;
     }

     private NewsPublicDetailView toPublicDetail(News news) {
          NewsPublicDetailView detail = new NewsPublicDetailView();
          detail.setId(news.getId());
          detail.setTitle(news.getTitle());
          detail.setSlug(news.getSlug());
          detail.setThumbnail(news.getThumbnail());
          detail.setSummary(news.getSummary());
          detail.setContentHtml(news.getContentHtml());
          detail.setCreatedAt(news.getCreatedAt());
          detail.setViewCount(news.getViewCount());
          return detail;
     }

     private int safeInt(Integer v) {
          return v == null ? 0 : v;
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
          if (v.isEmpty()) {
               throw new RuntimeException("Slug không hợp lệ");
          }
          return v;
     }
}
