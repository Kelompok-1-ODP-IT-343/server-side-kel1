package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.PropertyFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyFeatureRepository extends JpaRepository<PropertyFeature, Integer> {}
