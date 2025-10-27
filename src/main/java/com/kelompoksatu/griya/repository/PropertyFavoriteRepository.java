package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.PropertyFavorite;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyFavoriteRepository extends JpaRepository<PropertyFavorite, Long> {

  // ✅ Buat toggle favorite
  Optional<PropertyFavorite> findByUserIdAndPropertyId(Integer userId, Integer propertyId);

  boolean existsByUserIdAndPropertyId(Integer userId, Integer propertyId);

  // ✅ Tambahan untuk admin melihat semua favorites user
  @Query(
      value =
          """
        SELECT
            pf.id AS favorite_id,
            p.id AS property_id,
            p.title AS title,
            p.city AS city,
            p.price AS price,
            COALESCE(
                (SELECT pi.file_path
                 FROM property_images pi
                 WHERE pi.property_id = p.id AND pi.is_primary = TRUE
                 LIMIT 1),
                (SELECT pi.file_path
                 FROM property_images pi
                 WHERE pi.property_id = p.id
                 LIMIT 1)
            ) AS image_url,
            pf.created_at
        FROM property_favorites pf
        JOIN properties p ON pf.property_id = p.id
        WHERE pf.user_id = :userId
        ORDER BY pf.created_at DESC
    """,
      nativeQuery = true)
  List<Map<String, Object>> findFavoritesByUserId(@Param("userId") Integer userId);
}
