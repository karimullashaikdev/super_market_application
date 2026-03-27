package com.karim.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

	private final Cloudinary cloudinary;

	// Upload image → returns secure URL
	public String uploadImage(MultipartFile file) throws IOException {

		// ✅ validate format before uploading
		String originalName = file.getOriginalFilename().toLowerCase();
		if (!originalName.matches(".*\\.(jpg|jpeg|png|webp)$")) {
			throw new RuntimeException("Only JPG, PNG, WEBP images are allowed!");
		}

		Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "products"));
		return (String) result.get("secure_url");
	}
}
