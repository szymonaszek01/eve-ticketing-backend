package com.eve.ticketing.app.firebase;

import com.eve.ticketing.app.firebase.dto.FirebaseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Firebase", description = "Firebase management APIs")
@RequestMapping("/api/v1/firebase")
@RequiredArgsConstructor
@RestController
public class FirebaseController {

    private final FirebaseServiceImpl firebaseService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam(value = "file") MultipartFile file, @RequestHeader("Authorization") String token) {
        FirebaseDto firebaseDto = firebaseService.upload(file, token);
        return new ResponseEntity<>(firebaseDto, HttpStatus.OK);
    }
}
