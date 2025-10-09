package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for UserProfile entity
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    
    /**
     * Find user profile by user ID
     */
    Optional<UserProfile> findByUserId(Integer userId);
    
    /**
     * Find user profile by NIK
     */
    Optional<UserProfile> findByNik(String nik);
    
    /**
     * Find user profile by NPWP
     */
    Optional<UserProfile> findByNpwp(String npwp);
    
    /**
     * Check if NIK exists
     */
    boolean existsByNik(String nik);
    
    /**
     * Check if NPWP exists
     */
    boolean existsByNpwp(String npwp);
    
    /**
     * Check if user profile exists for user ID
     */
    boolean existsByUserId(Integer userId);
}