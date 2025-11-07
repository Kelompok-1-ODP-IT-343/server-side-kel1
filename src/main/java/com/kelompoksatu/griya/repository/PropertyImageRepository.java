package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Integer> {}
