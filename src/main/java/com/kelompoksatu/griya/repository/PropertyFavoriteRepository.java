package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.PropertyFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyFavoriteRepository extends JpaRepository<PropertyFavorite, Long> {

    Optional<PropertyFavorite> findByUserIdAndPropertyId(Integer userId, Integer propertyId);

    boolean existsByUserIdAndPropertyId(Integer userId, Integer propertyId);
}
