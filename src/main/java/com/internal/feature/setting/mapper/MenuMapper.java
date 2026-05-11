package com.internal.feature.setting.mapper;

import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.setting.dto.request.MenuCreateRequestDto;
import com.internal.feature.setting.dto.request.MenuUpdateRequestDto;
import com.internal.feature.setting.dto.response.AllMenuResponseDto;
import com.internal.feature.setting.dto.response.MenuItemDto;
import com.internal.feature.setting.dto.response.MenuResponseDto;
import com.internal.feature.setting.models.Menu;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MenuMapper {

    public MenuResponseDto toDto(Menu menu) {
        if (menu == null) {
            return null;
        }

        return MenuResponseDto.builder()
                .id(menu.getId())
                .title(menu.getTitle())
                .icon(menu.getIcon())
                .href(menu.getHref())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .displayOrder(menu.getDisplayOrder())
                .roles(menu.getRoles())
                .allowedUserIds(menu.getAllowedUsers() != null
                        ? menu.getAllowedUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .isActive(menu.getIsActive())
                .children(new ArrayList<>())
                .build();

    }

    public MenuItemDto toItemDto(Menu menu) {
        if (menu == null) {
            return null;
        }

        return MenuItemDto.builder()
                .id(menu.getId())
                .title(menu.getTitle())
                .icon(menu.getIcon())
                .href(menu.getHref())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .displayOrder(menu.getDisplayOrder())
                .roles(menu.getRoles())
                .isActive(menu.getIsActive())
                .build();
    }

    public Menu fromCreateDto(MenuCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Menu.builder()
                .title(dto.getTitle())
                .icon(dto.getIcon())
                .href(dto.getHref())
                .displayOrder(dto.getDisplayOrder())
                .roles(dto.getRoles() != null ? dto.getRoles() : new HashSet<>())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }

    public void updateFromDto(MenuUpdateRequestDto dto, Menu menu) {
        if (dto == null || menu == null) {
            return;
        }

        if (dto.getTitle() != null) {
            menu.setTitle(dto.getTitle());
        }
        if (dto.getIcon() != null) {
            menu.setIcon(dto.getIcon());
        }
        if (dto.getHref() != null) {
            menu.setHref(dto.getHref());
        }
        if (dto.getDisplayOrder() != null) {
            menu.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getRoles() != null) {
            menu.setRoles(dto.getRoles());
        }
        if (dto.getIsActive() != null) {
            menu.setIsActive(dto.getIsActive());
        }
    }

    public List<MenuResponseDto> buildMenuTree(List<Menu> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, MenuResponseDto> menuMap = new HashMap<>();
        List<MenuResponseDto> rootMenus = new ArrayList<>();

        // First pass: convert all to DTOs
        for (Menu menu : flatList) {
            MenuResponseDto dto = toDto(menu);
            dto.setChildren(new ArrayList<>()); // Initialize children list
            menuMap.put(dto.getId(), dto);
        }

        // Second pass: build tree structure
        for (MenuResponseDto dto : menuMap.values()) {
            if (dto.getParentId() == null) {
                rootMenus.add(dto);
            } else {
                MenuResponseDto parent = menuMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // Sort root menus and children
        rootMenus.sort(Comparator.comparing(MenuResponseDto::getDisplayOrder));
        for (MenuResponseDto root : rootMenus) {
            sortChildren(root);
        }

        return rootMenus;
    }

    private void sortChildren(MenuResponseDto menu) {
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            menu.getChildren().sort(Comparator.comparing(MenuResponseDto::getDisplayOrder));
            for (MenuResponseDto child : menu.getChildren()) {
                sortChildren(child);
            }
        }
    }

    public AllMenuResponseDto mapToListDto(List<MenuItemDto> content, Page<Menu> page) {
        return AllMenuResponseDto.builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public List<MenuItemDto> toDtoList(List<Menu> menus) {
        if (menus == null) {
            return new ArrayList<>();
        }
        return menus.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }
}
