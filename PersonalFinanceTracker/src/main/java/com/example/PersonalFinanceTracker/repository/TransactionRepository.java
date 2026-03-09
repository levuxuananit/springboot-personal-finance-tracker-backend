package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.Transaction;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
            AND c.type = 'INCOME'
            AND MONTH(t.date) = :month
            AND YEAR(t.date) = :year
            """)
    Double getTotalIncome(Long userId, int month, int year);

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
            AND c.type = 'EXPENSE'
            AND MONTH(t.date) = :month
            AND YEAR(t.date) = :year
            """)
    Double getTotalExpense(Long userId, int month, int year);


    @Query("""
                SELECT c.name, SUM(t.amount)
                FROM Transaction t
                JOIN t.category c
                WHERE t.user.id = :userId
                AND c.type = 'EXPENSE'
                AND MONTH(t.date) = :month
                AND YEAR(t.date) = :year
                GROUP BY c.name
                ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getTopExpenses(Long userId, int month, int year, Pageable pageable);


    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserTypeAndDateBetween(Long id, CategoryType categoryType, LocalDate startDate, LocalDate endDate);
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ─── Dashboard queries (date range → index-friendly) ────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.category.type = :type " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "ORDER BY t.date DESC")
    List<Transaction> findByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "ORDER BY t.date DESC LIMIT 3")
    List<Transaction> findTop3ByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ─── History query with pagination ──────────────────────────────────────

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:startDate IS NULL OR t.date >= :startDate) " +
            "AND (:endDate IS NULL OR t.date <= :endDate) " +
            "AND (:type IS NULL OR t.category.type = :type) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "ORDER BY t.date DESC")
    Page<Transaction> findHistory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    // Dùng cho report/category — lấy tất cả transaction theo type + date range
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.category.type = :type " +
            "AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Dùng cho report/monthly — lấy tất cả transaction trong cả năm
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndYear(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
>>>>>>> feature/create-transaction
}
