package com.cs447.securefiletransfer.backend.Repository;

import com.cs447.securefiletransfer.backend.Domain.File;
import com.cs447.securefiletransfer.backend.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByOwner(User owner);
}
