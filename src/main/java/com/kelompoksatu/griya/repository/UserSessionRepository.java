package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UserSession entity
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    /**
     * Find all sessions by user ID
     */
    List<UserSession> findByUserId(Integer userId);

    /**
     * Find active sessions by user ID (within last 24 hours)
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.lastActivity > :cutoffTime")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Integer userId,
                                                @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.lastActivity < :cutoffTime")
    void deleteExpiredSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete all sessions for a user
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
    void deleteAllSessionsByUserId(@Param("userId") Integer userId);

    /**
     * Update session last activity
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.lastActivity = :lastActivity WHERE s.id = :sessionId")
    void updateLastActivity(@Param("sessionId") String sessionId,
                           @Param("lastActivity") LocalDateTime lastActivity);
}
