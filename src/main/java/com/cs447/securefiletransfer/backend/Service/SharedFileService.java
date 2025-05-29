package com.cs447.securefiletransfer.backend.Service;

import com.cs447.securefiletransfer.backend.Domain.File;
import com.cs447.securefiletransfer.backend.Domain.SharedFile;
import com.cs447.securefiletransfer.backend.Domain.User;
import com.cs447.securefiletransfer.backend.Repository.FileRepository;
import com.cs447.securefiletransfer.backend.Repository.SharedFileRepository;
import com.cs447.securefiletransfer.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SharedFileService {

    private final SharedFileRepository sharedFileRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public String shareFile(Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String token = UUID.randomUUID().toString();

        SharedFile sharedFile = SharedFile.builder()
                .file(file)
                .sharedBy(owner)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        sharedFileRepository.save(sharedFile);
        return token;
    }

    @Transactional
    public String createTokenForFile(Long fileId, int expiresInHours) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User sharingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        File fileToShare = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        SharedFile sharedFile = SharedFile.builder()
                .token(UUID.randomUUID().toString())
                .file(fileToShare)
                .sharedBy(sharingUser)
                .expiresAt(LocalDateTime.now().plusHours(expiresInHours))
                .build();

        sharedFileRepository.save(sharedFile);
        return sharedFile.getToken();
    }

    public Optional<SharedFile> getSharedFile(String token) {
        return sharedFileRepository.findByToken(token)
                .filter(sf -> !isExpired(sf));
    }

    private boolean isExpired(SharedFile sf) {
        return sf.getExpiresAt() != null && sf.getExpiresAt().isBefore(LocalDateTime.now());
    }
}
