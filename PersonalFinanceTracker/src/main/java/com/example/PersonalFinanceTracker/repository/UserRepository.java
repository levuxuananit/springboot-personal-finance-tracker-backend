package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo email (dùng khi login)
    Optional<User> findByEmail(String email);
    // Kiểm tra email đã tồn tại chưa (dùng khi register)
    boolean existsByEmail(String email);
}
