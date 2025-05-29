package com.cs447.securefiletransfer.backend.Repository;

import com.cs447.securefiletransfer.backend.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
    }
