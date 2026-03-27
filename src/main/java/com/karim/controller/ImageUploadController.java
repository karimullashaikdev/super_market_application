package com.karim.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.karim.service.impl.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

	private final CloudinaryService cloudinaryService;

	@PostMapping("/upload")
	public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			// 1. send image to Cloudinary
			// 2. get back the URL
			// 3. return URL to whoever called this API
			String imageUrl = cloudinaryService.uploadImage(file);
			return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
		}
	}
}