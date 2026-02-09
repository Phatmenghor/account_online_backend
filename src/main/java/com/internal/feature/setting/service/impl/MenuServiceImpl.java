package com.internal.feature.setting.service.impl;

import com.internal.enumation.RoleEnum;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.auth.models.Role;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.feature.setting.dto.request.AssignMenuToUserRequestDto;
import com.internal.feature.setting.dto.request.AssignUserMenusRequestDto;
import com.internal.feature.setting.dto.request.GetAllMenuRequestDto;
import com.internal.feature.setting.dto.request.MenuCreateRequestDto;
import com.internal.feature.setting.dto.request.MenuUpdateRequestDto;
import com.internal.feature.setting.dto.response.AllMenuResponseDto;
import com.internal.feature.setting.dto.response.MenuItemDto;
import com.internal.feature.setting.dto.response.MenuResponseDto;
import com.internal.feature.setting.mapper.MenuMapper;
import com.internal.feature.setting.models.Menu;
import com.internal.feature.setting.repository.MenuRepository;
import com.internal.feature.setting.service.MenuService;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final MenuMapper menuMapper;
    private final SecurityUtils securityUtils;

    @Override
    public List<MenuResponseDto> getMenusByCurrentUser() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return getMenusByUserId(currentUser.getId());
    }

    @Override
    public List<MenuResponseDto> getMenusByUserId(Long userId) {
        log.debug("Getting menus for user ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get user's roles
        Set<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.debug("User roles: {}", userRoles);

        // DEVELOPER gets ALL menus, but respecting explicit exclusions
        if (userRoles.contains(RoleEnum.DEVELOPER)) {
            log.debug("User is DEVELOPER, returning all menus (respecting exclusions)");
            List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
            
            // Filter out menus where user is explicitly excluded
            List<Menu> filteredMenus = allMenus.stream()
                    .filter(menu -> !menu.getExcludedUsers().contains(user))
                    .collect(Collectors.toList());
                    
            return menuMapper.buildMenuTree(filteredMenus);
        }

        // Get menus accessible to user
        List<Menu> accessibleMenus = getAccessibleMenusForUser(user, userRoles);
        log.debug("Found {} accessible menus", accessibleMenus.size());

        return menuMapper.buildMenuTree(accessibleMenus);
    }

    @Override
    public AllMenuResponseDto getAllMenus(GetAllMenuRequestDto request) {
        log.debug("Getting all menus with filters: {}", request);

        Pageable pageable = PageRequest.of(
                Math.max(request.getPageNo() - 1, 0),
                Math.max(request.getPageSize(), 1)
        );

        Page<Menu> page = menuRepository.findByFilters(
                request.getSearch(),
                request.getIsActive(),
                request.getParentId(),
                pageable
        );

        log.debug("Found {} menus on page {} of {}", page.getNumberOfElements(),
                request.getPageNo(), page.getTotalPages());

        List<MenuItemDto> content = menuMapper.toDtoList(page.getContent());

        return menuMapper.mapToListDto(content, page);
    }

    @Override
    public MenuResponseDto getMenuById(Long id) {
        log.debug("Getting menu by ID: {}", id);
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found with ID: " + id));
        return menuMapper.toDto(menu);
    }

    @Override
    @Transactional
    public MenuResponseDto createMenu(MenuCreateRequestDto request) {
        log.info("Creating new menu: {}", request.getTitle());

        Menu menu = menuMapper.fromCreateDto(request);

        // Handle parent menu - Option 1: Use existing parent
        if (request.getParentId() != null) {
            log.debug("Using existing parent menu with ID: {}", request.getParentId());
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent menu not found with ID: " + request.getParentId()));
            menu.setParent(parent);
        }
        // Handle parent menu - Option 2: Create new parent
        else if (request.getParentMenu() != null) {
            log.info("Creating new parent menu: {}", request.getParentMenu().getTitle());
            
            // Create and save the parent menu first
            Menu parentMenu = Menu.builder()
                    .title(request.getParentMenu().getTitle())
                    .icon(request.getParentMenu().getIcon())
                    .href(request.getParentMenu().getHref())
                    .displayOrder(request.getParentMenu().getDisplayOrder())
                    .roles(request.getParentMenu().getRoles())
                    .isActive(request.getParentMenu().getIsActive())
                    .build();
            
            Menu savedParent = menuRepository.save(parentMenu);
            log.info("Created parent menu with ID: {}", savedParent.getId());
            
            menu.setParent(savedParent);
        }
        // If neither is provided, this is a root menu
        else {
            log.debug("Creating root menu (no parent)");
        }

        // Set allowed users if provided
        if (request.getAllowedUserIds() != null && !request.getAllowedUserIds().isEmpty()) {
            Set<UserEntity> users = new HashSet<>(userRepository.findAllById(request.getAllowedUserIds()));
            menu.setAllowedUsers(users);
        }

        Menu savedMenu = menuRepository.save(menu);
        log.info("Created menu with ID: {}", savedMenu.getId());

        return menuMapper.toDto(savedMenu);
    }

    @Override
    @Transactional
    public MenuResponseDto updateMenu(Long id, MenuUpdateRequestDto request) {
        log.info("Updating menu with ID: {}", id);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found with ID: " + id));

        menuMapper.updateFromDto(request, menu);

        // Update parent if provided
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Menu cannot be its own parent");
            }
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent menu not found with ID: " + request.getParentId()));
            menu.setParent(parent);
        }

        // Update allowed users if provided
        if (request.getAllowedUserIds() != null) {
            Set<UserEntity> users = new HashSet<>(userRepository.findAllById(request.getAllowedUserIds()));
            menu.setAllowedUsers(users);
        }

        Menu updatedMenu = menuRepository.save(menu);
        log.info("Updated menu with ID: {}", updatedMenu.getId());

        return menuMapper.toDto(updatedMenu);
    }

    @Override
    @Transactional
    public MenuResponseDto deleteMenu(Long id) {
        log.info("Deleting menu with ID: {}", id);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found with ID: " + id));

        menu.setIsActive(false);
        Menu deletedMenu = menuRepository.save(menu);

        log.info("Deleted menu with ID: {}", id);
        return menuMapper.toDto(deletedMenu);
    }

    @Override
    @Transactional
    public MenuResponseDto assignMenuToUsers(Long menuId, AssignMenuToUserRequestDto request) {
        log.info("Assigning menu {} to users: {}", menuId, request.getUserIds());

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu not found with ID: " + menuId));

        List<UserEntity> users = userRepository.findAllById(request.getUserIds());
        menu.getAllowedUsers().addAll(users);

        Menu updatedMenu = menuRepository.save(menu);
        log.info("Assigned menu {} to {} users", menuId, users.size());

        return menuMapper.toDto(updatedMenu);
    }

    @Override
    @Transactional
    public MenuResponseDto removeMenuFromUsers(Long menuId, List<Long> userIds) {
        log.info("Removing menu {} from users: {}", menuId, userIds);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu not found with ID: " + menuId));

        menu.getAllowedUsers().removeIf(user -> userIds.contains(user.getId()));

        Menu updatedMenu = menuRepository.save(menu);
        log.info("Removed menu {} from {} users", menuId, userIds.size());

        return menuMapper.toDto(updatedMenu);
    }

    @Override
    @Transactional
    public List<MenuResponseDto> assignMenusToUser(AssignUserMenusRequestDto request) {
        log.info("Assigning menus to user ID: {}", request.getUserId());

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));

        List<Menu> allMenus = menuRepository.findAll();
        List<Long> requestedMenuIds = request.getMenuIds();

        for (Menu menu : allMenus) {
            boolean shouldHaveAccess = requestedMenuIds.contains(menu.getId());
            boolean currentlyHasAccess = menu.getAllowedUsers().contains(user);
            boolean currentlyExcluded = menu.getExcludedUsers().contains(user);

            if (shouldHaveAccess) {
                // If user should have access:
                // 1. Add to allowedUsers if not present
                if (!currentlyHasAccess) {
                    menu.getAllowedUsers().add(user);
                }
                // 2. Remove from excludedUsers if present (to un-ban them)
                if (currentlyExcluded) {
                    menu.getExcludedUsers().remove(user);
                }
                menuRepository.save(menu);
            } else {
                // If user should NOT have access:
                // 1. Remove from allowedUsers if present
                if (currentlyHasAccess) {
                    menu.getAllowedUsers().remove(user);
                }
                // 2. Add to excludedUsers if not present (to explicitly ban them)
                if (!currentlyExcluded) {
                    menu.getExcludedUsers().add(user);
                }
                menuRepository.save(menu);
            }
        }

        // Return updated menu list for the user
        return getMenusByUserId(user.getId());
    }

    // Private helper methods

    private List<Menu> getAccessibleMenusForUser(UserEntity user, Set<RoleEnum> userRoles) {
        // Get all active menus
        List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // Filter menus
        return allMenus.stream()
                .filter(menu -> isMenuAccessibleToUser(menu, user, userRoles))
                .collect(Collectors.toList());
    }

    private boolean isMenuAccessibleToUser(Menu menu, UserEntity user, Set<RoleEnum> userRoles) {
        // Check explicit exclusion first (BLACK LIST)
        if (menu.getExcludedUsers().contains(user)) {
            return false;
        }

        // Check user-specific access (WHITE LIST)
        if (menu.getAllowedUsers().contains(user)) {
            return true;
        }

        // Check role-based access
        if (menu.getRoles() == null || menu.getRoles().isEmpty()) {
            return false;
        }

        for (RoleEnum menuRole : menu.getRoles()) {
            if (userRoles.contains(menuRole)) {
                return true;
            }
        }

        return false;
    }
}
