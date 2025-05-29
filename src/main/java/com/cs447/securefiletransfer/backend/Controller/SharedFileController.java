package com.cs447.securefiletransfer.backend.Controller;

import com.cs447.securefiletransfer.backend.Domain.File;
import com.cs447.securefiletransfer.backend.Domain.SharedFile;
import com.cs447.securefiletransfer.backend.Service.FileService;
import com.cs447.securefiletransfer.backend.Service.SharedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class SharedFileController {

    private final SharedFileService sharedFileService;
    private final FileService fileService;

    @PostMapping("/generate/{fileId}")
    public ResponseEntity<String> shareFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String token = sharedFileService.shareFile(fileId, userDetails.getUsername());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/download/shared/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token) {
        SharedFile sharedFile = sharedFileService.getSharedFile(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        File file = sharedFile.getFile();
        Resource resource = fileService.getFileAsResource(file.getFilePath(), false);

        String originalFileName = file.getName();

        String asciiFileName = originalFileName.replaceAll("[^\\x20-\\x7E]", "_");

        String encodedFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);

        String contentDisposition = "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }


    @PostMapping("/shared-files/create")
    public ResponseEntity<Map<String, String>> createSharedFileToken(@RequestBody Map<String, Object> request) {
        Long fileId = Long.valueOf(request.get("fileId").toString());
        int expiresInDays = Integer.parseInt(request.getOrDefault("expiresInDays", 1).toString());

        String token = sharedFileService.createTokenForFile(fileId, expiresInDays);

        return ResponseEntity.ok(Map.of("token", token));
    }
}
