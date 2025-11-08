package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.SystemNotification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

  // Mencari notifikasi berdasarkan user_id, diurutkan dari yang terbaru
  List<SystemNotification> findByUserIdOrderByCreatedAtDesc(Integer userId);

  // Menghitung notifikasi yang belum dibaca oleh user
  long countByUserIdAndReadAtIsNull(Integer userId);
}
