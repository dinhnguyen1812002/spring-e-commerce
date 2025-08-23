package com.app.e_commerce.services;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    // 📂 Thư mục mặc định lưu ảnh (nằm trong thư mục gốc project)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File rỗng, không thể upload.");
        }

        // ✅ Tạo thư mục nếu chưa có
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // ✅ Sinh tên file duy nhất
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // ✅ Ghi file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối để lưu DB (dùng hiển thị ảnh)
        return "uploads/" + fileName;
    }
}
