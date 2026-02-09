package com.internal.feature.setting.repository;

import com.internal.feature.setting.models.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Menu> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    // Find menu by title when it has no parent (root menu)
    Menu findByTitleAndParentIsNull(String title);

    // Find menu by title and parent (for child menus)
    Menu findByTitleAndParent(String title, Menu parent);

    @Query("SELECT m FROM Menu m WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isActive IS NULL OR m.isActive = :isActive) AND " +
           "(:parentId IS NULL OR (:parentId = 0L AND m.parent IS NULL) OR m.parent.id = :parentId) " +
           "ORDER BY m.displayOrder ASC")
    Page<Menu> findByFilters(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("parentId") Long parentId,
            Pageable pageable);

    @Query("SELECT DISTINCT m FROM Menu m LEFT JOIN m.allowedUsers u WHERE " +
           "m.isActive = true AND (m.parent IS NULL OR m.parent.id = 0) AND u.id = :userId " +
           "ORDER BY m.displayOrder ASC")
    List<Menu> findActiveRootMenusByUserId(@Param("userId") Long userId);
}
