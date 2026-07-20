package com.example.springboot.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUploadUtils {
    private FileUploadUtils() {
    }

    public static Path uploadRoot(String uploadDir) {
        String configured = uploadDir == null || uploadDir.trim().isEmpty()
                ? "uploads"
                : uploadDir.trim();
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    public static Path uploadSubdir(String uploadDir, String subdir) {
        return uploadRoot(uploadDir).resolve(subdir).normalize();
    }
}
