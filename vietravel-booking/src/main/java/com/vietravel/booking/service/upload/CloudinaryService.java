package com.vietravel.booking.service.upload;

import com.cloudinary.Cloudinary;
import com.vietravel.booking.config.CloudinaryProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties props;

    public CloudinaryService(Cloudinary cloudinary, CloudinaryProperties props) {
        this.cloudinary = cloudinary;
        this.props = props;
    }

    public String uploadUserAvatar(Long userId, MultipartFile file) {
        try {
            Map<String, Object> options = new HashMap<>();
            if (props.getFolder() != null && !props.getFolder().isBlank()) {
                options.put("folder", props.getFolder());
            }
            options.put("public_id", "user_" + userId);
            options.put("overwrite", true);
            options.put("resource_type", "image");

            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = res.get("secure_url");
            Object url = res.get("url");
            String out = (secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : ""));
            if (out.isBlank())
                throw new RuntimeException("Cloudinary không trả url");
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Upload Cloudinary thất bại: " + e.getMessage());
        }
    }

    public String uploadTourImage(String name, MultipartFile file) {
        try {
            Map<String, Object> options = new HashMap<>();
            String baseFolder = (props.getFolder() != null && !props.getFolder().isBlank())
                    ? props.getFolder() + "/tours"
                    : "tours";
            options.put("folder", baseFolder);
            String safeName = (name == null || name.isBlank()) ? "tour" : name.replaceAll("[^a-zA-Z0-9_-]", "_");
            options.put("public_id", safeName + "_" + System.currentTimeMillis());
            options.put("overwrite", true);
            options.put("resource_type", "image");

            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = res.get("secure_url");
            Object url = res.get("url");
            String out = (secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : ""));
            if (out.isBlank())
                throw new RuntimeException("Cloudinary không trả url");
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Upload Cloudinary thất bại: " + e.getMessage());
        }
    }

    public String uploadCampaignBanner(String name, MultipartFile file) {
        try {
            Map<String, Object> options = new HashMap<>();
            String baseFolder = (props.getFolder() != null && !props.getFolder().isBlank())
                    ? props.getFolder() + "/campaigns"
                    : "campaigns";
            options.put("folder", baseFolder);
            String safeName = (name == null || name.isBlank()) ? "campaign" : name.replaceAll("[^a-zA-Z0-9_-]", "_");
            options.put("public_id", safeName + "_" + System.currentTimeMillis());
            options.put("overwrite", true);
            options.put("resource_type", "image");

            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = res.get("secure_url");
            Object url = res.get("url");
            String out = (secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : ""));
            if (out.isBlank())
                throw new RuntimeException("Cloudinary không trả url");
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Upload Cloudinary thất bại: " + e.getMessage());
        }
    }
}
