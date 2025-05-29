package com.cs447.securefiletransfer.backend.Service;

import com.cs447.securefiletransfer.backend.Domain.File;
import com.cs447.securefiletransfer.backend.Domain.SharedFile;
import com.cs447.securefiletransfer.backend.Domain.User;
import com.cs447.securefiletransfer.backend.Repository.FileRepository;
import com.cs447.securefiletransfer.backend.Repository.SharedFileRepository;
import com.cs447.securefiletransfer.backend.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final SharedFileRepository sharedFileRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    LocalDateTime dateTime = LocalDateTime.now();
    Instant instant = dateTime.toInstant(ZoneOffset.UTC);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir).normalize();
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder", e);
        }
    }

    public File store(MultipartFile file, String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("Original filename is missing");
            }

            int extIndex = originalFilename.lastIndexOf(".");
            String fileExtension = extIndex == -1 ? "" : originalFilename.substring(extIndex);
            String newFilename = UUID.randomUUID() + fileExtension;

            Path destinationFile = rootLocation.resolve(newFilename).normalize();
            Files.copy(file.getInputStream(), destinationFile);

            File fileEntity = File.builder()
                    .name(originalFilename)
                    .type(file.getContentType())
                    .filePath(newFilename)
                    .size(file.getSize())
                    .owner(user)
                    .build();

            return fileRepository.save(fileEntity);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Path load(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        if (!file.startsWith(rootLocation)) {
            throw new RuntimeException("Invalid file path: " + filename);
        }
        return file;
    }

    public List<File> loadAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return fileRepository.findByOwner(user);
    }

    public void deleteFile(Long fileId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized file deletion");
        }

        try {
            Files.deleteIfExists(load(file.getFilePath()));
            fileRepository.delete(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
    public Resource getFileAsResource(String tokenOrFilename, boolean isToken) {
        try {
            Path filePath;

            if (isToken) {
                SharedFile sharedFile = sharedFileRepository.findByToken(tokenOrFilename)
                        .orElseThrow(() -> new RuntimeException("Invalid token"));

                if (sharedFile.getExpiresAt().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Token has expired");
                }

                File file = sharedFile.getFile();
                filePath = rootLocation.resolve(file.getFilePath()).normalize();
            } else {
                filePath = load(tokenOrFilename);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or unreadable");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }
}