package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ImageAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageAdminRepository extends JpaRepository<ImageAdmin, Long> {
}
