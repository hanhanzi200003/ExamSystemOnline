package com.example.exam_system.login.repository;

import com.example.exam_system.login.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByStudentStaffId(String studentStaffId);

    Page<User> findByRole(User.UserRole role, Pageable pageable);

    long countByRole(User.UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<User> findByRoleAndKeyword(@Param("role") User.UserRole role, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
