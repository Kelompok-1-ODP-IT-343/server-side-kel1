package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.PropertyFavorite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyFavoriteRepository extends JpaRepository<PropertyFavorite, Long> {

  Optional<PropertyFavorite> findByUserIdAndPropertyId(Integer userId, Integer propertyId);

  boolean existsByUserIdAndPropertyId(Integer userId, Integer propertyId);
}
