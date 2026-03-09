package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user theo email (dùng khi login)
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    // Kiểm tra email đã tồn tại chưa (dùng khi register)


}
