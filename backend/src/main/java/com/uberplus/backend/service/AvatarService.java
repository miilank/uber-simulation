package com.uberplus.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AvatarService {
    String storeAvatar(MultipartFile avatar) throws IOException;

    Resource getAvatar(String filename) throws IOException;

    void deleteAvatar(String filename) throws IOException;
}
