package com.bukimind.ai.chatbox.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileUploadService {
    // Cloudinary instance is injected by Spring
    @Autowired
    private Cloudinary cloudinary;

    // Uploads a file (image, PDF, video, etc.) to Cloudinary
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty!");
        }

        // Upload the file to Cloudinary
        // resource_type = "auto" allows uploading images, PDFs, videos, audio automatically
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(), // convert file to byte array
                ObjectUtils.asMap(
                        "folder", folder,  // Cloudinary folder
                        "resource_type", "auto" // auto-detect file type
                )
        );

        // Return the secure URL of the uploaded file
        return uploadResult.get("secure_url").toString();
    }
}
