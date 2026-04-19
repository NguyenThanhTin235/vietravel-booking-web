package com.vietravel.booking.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig{

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties p){
        if(p.getCloudName()==null||p.getCloudName().isBlank()
                ||p.getApiKey()==null||p.getApiKey().isBlank()
                ||p.getApiSecret()==null||p.getApiSecret().isBlank()){
            throw new IllegalStateException("Cloudinary config thiếu (cloud-name/api-key/api-secret). Kiểm tra application.yml");
        }

        Map<String,String> cfg=new HashMap<>();
        cfg.put("cloud_name",p.getCloudName());
        cfg.put("api_key",p.getApiKey());
        cfg.put("api_secret",p.getApiSecret());
        cfg.put("secure", "true");
        return new Cloudinary(cfg);
    }
}
