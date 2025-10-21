package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for User entity */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  /** Find user by username */
  Optional<User> findByUsername(String username);

  /** Find user by email */
  Optional<User> findByEmail(String email);

  /** Find user by phone */
  Optional<User> findByPhone(String phone);

  /** Find user by username or email */
  @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
  Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

  /** Check if username exists */
  boolean existsByUsername(String username);

  /** Check if email exists */
  boolean existsByEmail(String email);

  /** Check if phone exists */
  boolean existsByPhone(String phone);

  /** Update last login timestamp */
  @Modifying
  @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
  void updateLastLoginAt(
      @Param("userId") Integer userId, @Param("loginTime") LocalDateTime loginTime);

  /** Update failed login attempts */
  @Modifying
  @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
  void updateFailedLoginAttempts(
      @Param("userId") Integer userId, @Param("attempts") Integer attempts);

  /** Lock user account until specified time */
  @Modifying
  @Query("UPDATE User u SET u.lockedUntil = :lockTime WHERE u.id = :userId")
  void lockUserAccount(@Param("userId") Integer userId, @Param("lockTime") LocalDateTime lockTime);

  /** Unlock user account */
  @Modifying
  @Query("UPDATE User u SET u.lockedUntil = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
  void unlockUserAccount(@Param("userId") Integer userId);

  /** Verify email */
  @Modifying
  @Query("UPDATE User u SET u.emailVerifiedAt = :verificationTime WHERE u.id = :userId")
  void verifyEmail(
      @Param("userId") Integer userId, @Param("verificationTime") LocalDateTime verificationTime);

  /** Verify phone */
  @Modifying
  @Query("UPDATE User u SET u.phoneVerifiedAt = :verificationTime WHERE u.id = :userId")
  void verifyPhone(
      @Param("userId") Integer userId, @Param("verificationTime") LocalDateTime verificationTime);

  @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
  Optional<User> findByUsernameWithRole(@Param("username") String username);
}
