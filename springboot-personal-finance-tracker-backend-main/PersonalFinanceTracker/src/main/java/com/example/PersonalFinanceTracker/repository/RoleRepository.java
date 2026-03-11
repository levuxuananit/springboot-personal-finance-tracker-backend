package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository xử lý Role
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}