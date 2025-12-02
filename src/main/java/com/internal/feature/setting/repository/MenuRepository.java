package com.internal.feature.setting.repository;

import com.internal.feature.setting.models.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    List<Menu> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Menu> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    // Find menu by title when it has no parent (root menu)
    Menu findByTitleAndParentIsNull(String title);

    // Find menu by title and parent (for child menus)
    Menu findByTitleAndParent(String title, Menu parent);
}
