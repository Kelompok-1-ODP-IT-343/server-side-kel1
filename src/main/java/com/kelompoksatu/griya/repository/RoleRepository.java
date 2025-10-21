package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for Role entity */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

  /** Find role by name */
  Optional<Role> findByName(String name);

  /** Check if role name exists */
  boolean existsByName(String name);
}
