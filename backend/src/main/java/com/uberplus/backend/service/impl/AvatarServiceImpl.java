package com.uberplus.backend.service.impl;

import com.uberplus.backend.service.AvatarService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
public class AvatarServiceImpl implements AvatarService {
    private static final long MAX_AVATAR_BYTES = 12 * 1024 * 1024;
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp"};

    @Value("${app.file-storage.base-dir}")
    private String baseDir;

    @Override
    public String storeAvatar(MultipartFile avatar) throws IOException {
//        if (avatar.getSize() > MAX_AVATAR_BYTES) {
//            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
//                    "Maximum avatar size is 12MB.");
//        }

        boolean allowed = Arrays.stream(ALLOWED_CONTENT_TYPES)
                .anyMatch(type -> type.equalsIgnoreCase(avatar.getContentType()));
        if (!allowed)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "File type must be jpeg, png, or webp.");


        try (InputStream is = avatar.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "Uploaded file is not a valid image.");
            }
        }

        String ext = extensionFromContentType(avatar.getContentType());
        String filename = UUID.randomUUID().toString() + "." + ext;
        Files.createDirectories(avatarsDir());

        Path target = avatarsDir().resolve(filename);
        avatar.transferTo(target.toFile());

        return filename;
    }

    @Override
    public Resource getAvatar(String filename) throws IOException {
        Path file = avatarsDir().resolve(filename).normalize();
        if (!Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.");
        }

        return new UrlResource(file.toUri());
    }

    @Override
    public void deleteAvatar(String filename) throws IOException {
        if(filename.equals("defaultprofile.png")) {
            return;
        }
        Path file = avatarsDir().resolve(filename).normalize();
        Files.delete(file);
    }

    private final Path avatarsDir() {
        return Paths.get(baseDir, "avatars").toAbsolutePath().normalize();
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default ->
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                            "File type must be jpeg, png, or webp.");
        };
    }
}
