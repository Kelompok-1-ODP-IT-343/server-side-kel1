package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.UserSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for UserSession entity */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

  /** Delete expired sessions */
  @Modifying
  @Query("DELETE FROM UserSession s WHERE s.lastActivity < :cutoffTime")
  void deleteExpiredSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

  /** Delete all sessions for a user */
  @Modifying
  @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
  void deleteAllSessionsByUserId(@Param("userId") Integer userId);

  /** Update session last activity */
  @Modifying
  @Query("UPDATE UserSession s SET s.lastActivity = :lastActivity WHERE s.id = :sessionId")
  void updateLastActivity(
      @Param("sessionId") String sessionId, @Param("lastActivity") LocalDateTime lastActivity);

  @Query(
      "SELECT COUNT(us) > 0 FROM UserSession us "
          + "WHERE us.refreshToken = :refreshToken AND us.status = 'ACTIVE'")
  boolean existsActiveRefreshToken(String refreshToken);

  @Query(
      "SELECT us FROM UserSession us WHERE us.refreshToken = :refreshToken AND us.status = 'ACTIVE'")
  Optional<UserSession> findActiveByRefreshToken(String refreshToken);

  @Modifying
  @Query("delete from UserProfile up where up.userId = :userId")
  void deleteByUserId(@Param("userId") Integer userId);
}
