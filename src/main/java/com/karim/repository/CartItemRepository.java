package com.karim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.karim.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ---------------- View Active Cart ----------------
    List<CartItem> findByUserIdAndDeletedFalse(Long userId);

    // ---------------- Find Active Item ----------------
    Optional<CartItem> findByUserIdAndProductIdAndDeletedFalse(Long userId, Long productId);

    // 🔥 NEW: Find Even If Deleted (Important Fix)
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    // ---------------- Soft Delete All ----------------
    @Modifying
    @Query("UPDATE CartItem c SET c.deleted = true WHERE c.userId = :userId")
    void deleteByUserId(Long userId);

    // ---------------- Find By Id Active ----------------
    Optional<CartItem> findByIdAndDeletedFalse(Long id);
}
