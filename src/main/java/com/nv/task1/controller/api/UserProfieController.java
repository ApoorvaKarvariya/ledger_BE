package com.nv.task1.controller.api;

import com.nv.task1.entity.User;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "photo", user.getPhoto() != null ? user.getPhoto() : ""
        ));
    }

    @PostMapping("/photo")
    public ResponseEntity<?> uploadPhoto(@RequestBody Map<String, String> body, Authentication auth) {
        String photo = body.get("photo");
        if (photo == null || photo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No photo provided"));
        }
        // ~2MB safety limit on the base64 string, keeps DB rows sane
        if (photo.length() > 2_000_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image too large. Please use a smaller photo."));
        }
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        user.setPhoto(photo);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Photo updated", "photo", photo));
    }
}