package com.example.PersonalFinanceTracker.testutil;

import com.example.PersonalFinanceTracker.dto.CategoryRequestDTO;
import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.dto.TransactionRequestDTO;
import com.example.PersonalFinanceTracker.dto.TransactionResponseDTO;
import com.example.PersonalFinanceTracker.dto.auth.LoginRequest;
import com.example.PersonalFinanceTracker.dto.auth.RegisterRequest;
import com.example.PersonalFinanceTracker.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataFactory {

    // ─── Entity builders ─────────────────────────────────────────────────────

    public static Role buildRole(Long id, String name) {
        Role r = new Role();
        r.setId(id);
        r.setName(name);
        return r;
    }

    public static User buildUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setFullName("Nguyen Van A");
        u.setEmail(email);
        u.setPassword("$2a$10$hashed");
        u.setRole(buildRole(1L, "USER"));
        return u;
    }

    public static CategoryIcon buildCategoryIcon(Long id, String name, String emoji, String iconUrl) {
        CategoryIcon ci = new CategoryIcon();
        ci.setId(id);
        ci.setCategoryName(name);
        ci.setEmoji(emoji);
        ci.setIconUrl(iconUrl);
        return ci;
    }

    public static Category buildCategory(Long id, User user, CategoryIcon icon, CategoryType type) {
        Category c = new Category();
        c.setId(id);
        c.setUser(user);
        c.setCategoryIcon(icon);
        c.setType(type);
        return c;
    }

    public static Transaction buildTransaction(Long id, User user, Category category,
                                               BigDecimal amount, LocalDate date) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUser(user);
        t.setCategory(category);
        t.setAmount(amount);
        t.setDate(date);
        t.setNote("test note");
        return t;
    }

    // ─── DTO builders ─────────────────────────────────────────────────────────

    public static CategoryResponseDTO buildCategoryResponseDTO(Long id, String name,
                                                               String emoji, String iconUrl,
                                                               String type) {
        return new CategoryResponseDTO(id, name, emoji, iconUrl, type);
    }

    public static TransactionResponseDTO buildTransactionResponseDTO(Long id, BigDecimal amount,
                                                                     String note, String date,
                                                                     CategoryResponseDTO category) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(id);
        dto.setAmount(amount);
        dto.setNote(note);
        dto.setDate(date);
        dto.setCategory(category);
        return dto;
    }

    // ─── Request builders ─────────────────────────────────────────────────────

    public static RegisterRequest buildRegisterRequest(String fullName, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setFullName(fullName);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    public static LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ─── Shortcuts với data mặc định ─────────────────────────────────────────

    public static User defaultUser() {
        return buildUser(1L, "user@example.com");
    }

    public static CategoryIcon foodIcon() {
        return buildCategoryIcon(1L, "Food", "🍔", "https://cdn.example.com/icons/food.png");
    }

    public static CategoryIcon salaryIcon() {
        return buildCategoryIcon(2L, "Salary", "💰", "https://cdn.example.com/icons/salary.png");
    }

    public static CategoryIcon otherIcon() {
        return buildCategoryIcon(99L, "Other", "❓", "https://cdn.example.com/icons/other.png");
    }

    public static Category expenseCategory(User user) {
        return buildCategory(1L, user, foodIcon(), CategoryType.EXPENSE);
    }

    public static Category incomeCategory(User user) {
        return buildCategory(2L, user, salaryIcon(), CategoryType.INCOME);
    }

    public static Transaction expenseTransaction(User user, Category category) {
        return buildTransaction(101L, user, category,
                new BigDecimal("120.00"), LocalDate.of(2024, 4, 24));
    }

    public static Transaction incomeTransaction(User user, Category category) {
        return buildTransaction(102L, user, category,
                new BigDecimal("2000.00"), LocalDate.of(2024, 4, 24));
    }

    // ─── CategoryRequestDTO builders ──────────────────────────────────────────

    public static CategoryRequestDTO buildCategoryRequest(String name, String type) {
        CategoryRequestDTO req = new CategoryRequestDTO();
        req.setName(name);
        req.setType(type);
        return req;
    }

    public static CategoryRequestDTO buildCategoryRequest(String name, String type, String emoji) {
        CategoryRequestDTO req = buildCategoryRequest(name, type);
        req.setEmoji(emoji);
        return req;
    }

    // ─── TransactionRequestDTO builders ──────────────────────────────────────

    public static TransactionRequestDTO buildTransactionRequest(BigDecimal amount,
                                                                String note,
                                                                Long categoryId,
                                                                String date) {
        TransactionRequestDTO req = new TransactionRequestDTO();
        req.setAmount(amount);
        req.setNote(note);
        req.setCategoryId(categoryId);
        req.setDate(date);
        return req;
    }

    public static TransactionRequestDTO buildTransactionRequest(BigDecimal amount,
                                                                Long categoryId,
                                                                String date) {
        return buildTransactionRequest(amount, null, categoryId, date);
    }
}
