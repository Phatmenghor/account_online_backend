package com.internal.feature.setting.controller;

import com.internal.config.RequiresRole;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.setting.dto.request.AssignMenuToUserRequestDto;
import com.internal.feature.setting.dto.request.AssignUserMenusRequestDto;
import com.internal.feature.setting.dto.request.GetAllMenuRequestDto;
import com.internal.feature.setting.dto.request.MenuCreateRequestDto;
import com.internal.feature.setting.dto.request.MenuUpdateRequestDto;
import com.internal.feature.setting.dto.response.AllMenuResponseDto;
import com.internal.feature.setting.dto.response.MenuResponseDto;
import com.internal.feature.setting.service.MenuService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/current")
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenusByCurrentUser() {
        log.debug("Fetching menus for current user");
        List<MenuResponseDto> menus = menuService.getMenusByCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.MENUS_RETRIEVED, menus));
    }

    @PostMapping("/user/{userId}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenusByUserId(@PathVariable Long userId) {
        log.debug("Fetching menus for user ID: {}", userId);
        List<MenuResponseDto> menus = menuService.getMenusByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_MENUS_RETRIEVED, menus));
    }

    @PostMapping
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<AllMenuResponseDto>> getAllMenus(@RequestBody GetAllMenuRequestDto request) {
        log.info("Fetching all menus with filters: {}", request);
        AllMenuResponseDto result = menuService.getAllMenus(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_MENUS), result));
    }

    @PostMapping("/getById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> getMenuById(@PathVariable Long id) {
        log.debug("Fetching menu with ID: {}", id);
        MenuResponseDto menu = menuService.getMenuById(id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.MENU), menu));
    }

    @PostMapping("/create")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> createMenu(@Valid @RequestBody MenuCreateRequestDto request) {
        log.info("Creating new menu: {}", request.getTitle());
        MenuResponseDto menu = menuService.createMenu(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.MENU), menu));
    }

    @PostMapping("/updateById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> updateMenu(
            @PathVariable Long id,
            @RequestBody MenuUpdateRequestDto request) {
        log.info("Updating menu with ID: {}", id);
        MenuResponseDto menu = menuService.updateMenu(id, request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.MENU), menu));
    }

    @PostMapping("/deleteById/{id}")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> deleteMenu(@PathVariable Long id) {
        log.info("Deleting menu with ID: {}", id);
        MenuResponseDto menu = menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.MENU), menu));
    }

    @PostMapping("/{menuId}/assign-users")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> assignMenuToUsers(
            @PathVariable Long menuId,
            @Valid @RequestBody AssignMenuToUserRequestDto request) {
        log.info("Assigning menu {} to users", menuId);
        MenuResponseDto menu = menuService.assignMenuToUsers(menuId, request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERS_ASSIGNED_TO_MENU, menu));
    }

    @PostMapping("/{menuId}/remove-users")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<MenuResponseDto>> removeMenuFromUsers(
            @PathVariable Long menuId,
            @RequestBody List<Long> userIds) {
        log.info("Removing menu {} from users", menuId);
        MenuResponseDto menu = menuService.removeMenuFromUsers(menuId, userIds);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERS_REMOVED_FROM_MENU, menu));
    }

    @PostMapping("/user/assign-menus")
    @RequiresRole(value = {"DEVELOPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> assignMenusToUser(
            @Valid @RequestBody AssignUserMenusRequestDto request) {
        log.info("Assigning menus to user ID: {}", request.getUserId());
        List<MenuResponseDto> menus = menuService.assignMenusToUser(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.MENUS_ASSIGNED_TO_USER, menus));
    }
}
