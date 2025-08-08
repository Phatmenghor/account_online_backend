package com.internal.feature.auth.repository;

import com.internal.enumation.RoleEnum;
import com.internal.feature.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
    boolean existsByName(RoleEnum name);
}
