package com.cs447.securefiletransfer.backend.Repository;

import com.cs447.securefiletransfer.backend.Domain.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    Optional<SharedFile> findByToken(String token);
}
