package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
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

}
