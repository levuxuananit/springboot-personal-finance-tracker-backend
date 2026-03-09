package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> feature/budget-list

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
<<<<<<< HEAD
    // Tìm user theo email (dùng khi login)
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    // Kiểm tra email đã tồn tại chưa (dùng khi register)
=======
    Optional<User> findByEmail(String email);
>>>>>>> feature/budget-list
}
