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
        // Backwards compatible: store into configured uploadDir
        return storeFile(file, null);
    }

    // New: allow storing into a subfolder (e.g., "music" or "covers").
    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();

        Path uploadPath;
        if (subFolder == null || subFolder.isBlank()) {
            uploadPath = basePath;
        } else {
            // If configured uploadDir already points to a 'music' folder (common in this project),
            // use its parent as base so resolving subFolder yields src/.../uploads/<subFolder>
            Path fileName = basePath.getFileName();
            if (fileName != null && fileName.toString().equalsIgnoreCase("music")) {
                Path parent = basePath.getParent();
                if (parent != null) {
                    uploadPath = parent.resolve(subFolder).toAbsolutePath().normalize();
                } else {
                    uploadPath = basePath.resolveSibling(subFolder).toAbsolutePath().normalize();
                }
            } else {
                // normal case: treat uploadDir as base and append subFolder
                uploadPath = basePath.resolve(subFolder).toAbsolutePath().normalize();
            }
        }

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

    // Delete stored file (returns true if deleted)
    public boolean deleteFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) return false;
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();

            // Candidate locations to attempt deletion from (ordered):
            // 1) configured uploadDir/<filename>
            // 2) parent-of-configured-upload-dir/covers/<filename>
            // 3) ./uploads/music/<filename>
            // 4) ./uploads/covers/<filename>
            // 5) target/classes/static/uploads/music/<filename>
            // 6) target/classes/static/uploads/covers/<filename>

            Path p1 = basePath.resolve(filename).normalize();
            if (Files.exists(p1)) {
                return Files.deleteIfExists(p1);
            }

            Path parent = basePath.getParent();
            if (parent != null) {
                Path pCovers = parent.resolve("covers").resolve(filename).normalize();
                if (Files.exists(pCovers)) {
                    return Files.deleteIfExists(pCovers);
                }
            }

            Path p3 = Paths.get("./uploads/music").toAbsolutePath().normalize().resolve(filename);
            if (Files.exists(p3)) {
                return Files.deleteIfExists(p3);
            }

            Path p4 = Paths.get("./uploads/covers").toAbsolutePath().normalize().resolve(filename);
            if (Files.exists(p4)) {
                return Files.deleteIfExists(p4);
            }

            Path p5 = Paths.get("target/classes/static/uploads/music").toAbsolutePath().normalize().resolve(filename);
            if (Files.exists(p5)) {
                return Files.deleteIfExists(p5);
            }

            Path p6 = Paths.get("target/classes/static/uploads/covers").toAbsolutePath().normalize().resolve(filename);
            if (Files.exists(p6)) {
                return Files.deleteIfExists(p6);
            }

            return false;
        } catch (Exception e) {
            // Log can't be used directly here (no logger) to keep change minimal; caller can log
            return false;
        }
    }

}
