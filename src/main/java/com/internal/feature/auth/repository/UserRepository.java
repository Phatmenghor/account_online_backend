package com.internal.feature.auth.repository;

import com.internal.enumation.StatusData;
import com.internal.feature.auth.models.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

    Page<UserEntity> findByStatus(StatusData status, Pageable pageable);

    Page<UserEntity> findByStatusIn(List<StatusData> statuses, Pageable pageable);

    // search with statuses
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.status IN :statuses " +
            "AND (" +
            "  LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "  OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            ")")
    Page<UserEntity> searchByMultipleFieldsAndStatuses(
            @Param("searchText") String searchText,
            @Param("statuses")   List<StatusData> statuses,
            Pageable pageable);


    // search with status
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.status = :status " +
            "AND (" +
            "  LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "  OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            ")")
    Page<UserEntity> searchByMultipleFieldsAndStatus(
            @Param("searchText") String searchText,
            @Param("status")     StatusData status,
            Pageable pageable);

}
