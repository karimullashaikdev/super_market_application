package com.karim.repository;

import com.karim.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // All addresses for a user, newest first
    List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Fetch a specific address only if it belongs to this user (security check)
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    // Used before setting a new default — clears existing default
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);

    // Find the default address for checkout pre-fill
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}