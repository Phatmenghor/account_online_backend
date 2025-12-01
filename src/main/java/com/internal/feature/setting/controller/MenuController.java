package com.internal.feature.setting.controller;

import com.internal.config.RequiresRole;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.setting.dto.request.AssignMenuToUserRequestDto;
import com.internal.feature.setting.dto.request.GetAllMenuRequestDto;
import com.internal.feature.setting.dto.request.MenuCreateRequestDto;
import com.internal.feature.setting.dto.request.MenuUpdateRequestDto;
import com.internal.feature.setting.dto.response.AllMenuResponseDto;
import com.internal.feature.setting.dto.response.MenuResponseDto;
import com.internal.feature.setting.service.MenuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Menu Management")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/current")
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenusByCurrentUser() {
        log.debug("Fetching menus for current user");
        List<MenuResponseDto> menus = menuService.getMenusByCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Menus retrieved successfully", menus));
    }

    @PostMapping("/user/{userId}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenusByUserId(@PathVariable Long userId) {
        log.debug("Fetching menus for user ID: {}", userId);
        List<MenuResponseDto> menus = menuService.getMenusByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User menus retrieved successfully", menus));
    }

    @PostMapping
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<AllMenuResponseDto>> getAllMenus(@RequestBody GetAllMenuRequestDto request) {
        log.info("Fetching all menus with filters: {}", request);
        AllMenuResponseDto result = menuService.getAllMenus(request);
        return ResponseEntity.ok(ApiResponse.success("All menus retrieved successfully", result));
    }

    @PostMapping("/getById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> getMenuById(@PathVariable Long id) {
        log.debug("Fetching menu with ID: {}", id);
        MenuResponseDto menu = menuService.getMenuById(id);
        return ResponseEntity.ok(ApiResponse.success("Menu retrieved successfully", menu));
    }

    @PostMapping("/create")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> createMenu(@Valid @RequestBody MenuCreateRequestDto request) {
        log.info("Creating new menu: {}", request.getTitle());
        MenuResponseDto menu = menuService.createMenu(request);
        return ResponseEntity.ok(ApiResponse.success("Menu created successfully", menu));
    }

    @PostMapping("/updateById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> updateMenu(
            @PathVariable Long id,
            @RequestBody MenuUpdateRequestDto request) {
        log.info("Updating menu with ID: {}", id);
        MenuResponseDto menu = menuService.updateMenu(id, request);
        return ResponseEntity.ok(ApiResponse.success("Menu updated successfully", menu));
    }

    @PostMapping("/deleteById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> deleteMenu(@PathVariable Long id) {
        log.info("Deleting menu with ID: {}", id);
        MenuResponseDto menu = menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("Menu deleted successfully", menu));
    }

    @PostMapping("/{menuId}/assign-users")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> assignMenuToUsers(
            @PathVariable Long menuId,
            @Valid @RequestBody AssignMenuToUserRequestDto request) {
        log.info("Assigning menu {} to users", menuId);
        MenuResponseDto menu = menuService.assignMenuToUsers(menuId, request);
        return ResponseEntity.ok(ApiResponse.success("Users assigned to menu successfully", menu));
    }

    @PostMapping("/{menuId}/remove-users")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> removeMenuFromUsers(
            @PathVariable Long menuId,
            @RequestBody List<Long> userIds) {
        log.info("Removing menu {} from users", menuId);
        MenuResponseDto menu = menuService.removeMenuFromUsers(menuId, userIds);
        return ResponseEntity.ok(ApiResponse.success("Users removed from menu successfully", menu));
    }
}
