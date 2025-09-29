package com.music.musicstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1 && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(uniqueFilename);
        // Use REPLACE_EXISTING to be explicit; Files.copy will close streams
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    // Delete stored file (returns true if deleted or not found)
    public boolean deleteFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) return false;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();
            if (Files.exists(filePath)) {
                return Files.deleteIfExists(filePath);
            }
            return false;
        } catch (Exception e) {
            // Log can't be used directly here (no logger) to keep change minimal; caller can log
            return false;
        }
    }

}
