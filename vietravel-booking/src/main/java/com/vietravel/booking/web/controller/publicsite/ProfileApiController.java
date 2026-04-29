package com.vietravel.booking.web.controller.publicsite;

import com.vietravel.booking.service.auth.ProfileService;
import com.vietravel.booking.service.upload.CloudinaryService;
import com.vietravel.booking.web.dto.profile.ProfileResponse;
import com.vietravel.booking.web.dto.profile.ProfileUpdateRequest;
import com.vietravel.booking.web.dto.upload.CloudinaryUploadResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileApiController {

     private final ProfileService profileService;
     private final CloudinaryService cloudinaryService;

     public ProfileApiController(ProfileService profileService, CloudinaryService cloudinaryService) {
          this.profileService = profileService;
          this.cloudinaryService = cloudinaryService;
     }

     @GetMapping
     public ProfileResponse get() {
          return profileService.getCurrentProfile();
     }

     @PutMapping
     public ProfileResponse update(@RequestBody ProfileUpdateRequest req) {
          return profileService.updateCurrentProfile(req);
     }

     @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
     public CloudinaryUploadResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
          if (file == null || file.isEmpty())
               throw new RuntimeException("Vui lòng chọn file");

          ProfileResponse me = profileService.getCurrentProfile();
          String url = cloudinaryService.uploadUserAvatar(me.getId(), file);

          CloudinaryUploadResponse res = new CloudinaryUploadResponse();
          res.setUrl(url);
          res.setSecureUrl(url);
          return res;
     }

     @DeleteMapping("/avatar")
     public void deleteAvatar() {
          ProfileUpdateRequest req = new ProfileUpdateRequest();
          req.setAvatar(null);
          profileService.updateCurrentProfile(req);
     }
}
