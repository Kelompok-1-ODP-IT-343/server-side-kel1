package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for UserProfile entity */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

  /** Find user profile by user ID */
  Optional<UserProfile> findByUserId(Integer userId);

  /** Find user profile by NIK */
  Optional<UserProfile> findByNik(String nik);

  /** Find user profile by NPWP */
  Optional<UserProfile> findByNpwp(String npwp);

  /** Check if NIK exists */
  boolean existsByNik(String nik);

  /** Check if NPWP exists */
  boolean existsByNpwp(String npwp);

  /** Check if user profile exists for user ID */
  boolean existsByUserId(Integer userId);

  @Modifying
  @Query("DELETE FROM UserProfile up WHERE up.userId = :userId")
  int deleteAllByUserId(@Param("userId") Integer userId);
}
