package com.vietravel.booking.web.controller.publicsite;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController {

     @GetMapping(value = "/favicon.ico", produces = "image/x-icon")
     public ResponseEntity<byte[]> favicon() {
          return ResponseEntity.noContent().build();
     }
}
