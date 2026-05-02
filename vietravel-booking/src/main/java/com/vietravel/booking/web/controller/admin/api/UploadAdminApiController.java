package com.vietravel.booking.web.controller.admin.api;

import com.vietravel.booking.service.upload.CloudinaryService;
import com.vietravel.booking.web.dto.upload.CloudinaryUploadResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
public class UploadAdminApiController {

    private final CloudinaryService cloudinaryService;

    public UploadAdminApiController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/users/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CloudinaryUploadResponse uploadUserAvatar(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Vui lòng chọn file");

        String url = cloudinaryService.uploadUserAvatar(userId, file);

        CloudinaryUploadResponse res = new CloudinaryUploadResponse();
        res.setUrl(url);
        return res;
    }

    @PostMapping(value = "/tours/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CloudinaryUploadResponse uploadTourImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Vui lòng chọn file");

        String url = cloudinaryService.uploadTourImage(name, file);

        CloudinaryUploadResponse res = new CloudinaryUploadResponse();
        res.setUrl(url);
        return res;
    }

    @PostMapping(value = "/campaigns/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CloudinaryUploadResponse uploadCampaignBanner(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Vui lòng chọn file");

        String url = cloudinaryService.uploadCampaignBanner(name, file);

        CloudinaryUploadResponse res = new CloudinaryUploadResponse();
        res.setUrl(url);
        return res;
    }

    @PostMapping(value = "/news/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CloudinaryUploadResponse uploadNewsThumbnail(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Vui lòng chọn file");

        String url = cloudinaryService.uploadNewsThumbnail(name, file);

        CloudinaryUploadResponse res = new CloudinaryUploadResponse();
        res.setUrl(url);
        return res;
    }
}
