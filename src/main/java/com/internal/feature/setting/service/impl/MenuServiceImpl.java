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
import com.internal.feature.setting.specification.MenuSpec;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        // DEVELOPER gets ALL menus
        if (userRoles.contains(RoleEnum.DEVELOPER)) {
            log.debug("User is DEVELOPER, returning all menus");
            List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
            return menuMapper.buildMenuTree(allMenus);
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
                Math.max(request.getPageSize(), 1),
                Sort.by(Sort.Direction.ASC, "displayOrder")
        );

        Specification<Menu> spec = MenuSpec.searchByTitle(request.getSearch())
                .and(MenuSpec.hasStatus(request.getIsActive()))
                .and(MenuSpec.hasParentId(request.getParentId()));

        Page<Menu> page = menuRepository.findAll(spec, pageable);
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

        // Set parent if provided
        if (request.getParentId() != null) {
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent menu not found with ID: " + request.getParentId()));
            menu.setParent(parent);
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

            if (shouldHaveAccess && !currentlyHasAccess) {
                menu.getAllowedUsers().add(user);
                menuRepository.save(menu);
            } else if (!shouldHaveAccess && currentlyHasAccess) {
                menu.getAllowedUsers().remove(user);
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
        // Check user-specific access first
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
