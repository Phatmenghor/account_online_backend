package com.internal.feature.setting.service;

import com.internal.feature.setting.dto.request.AssignMenuToUserRequestDto;
import com.internal.feature.setting.dto.request.AssignUserMenusRequestDto;
import com.internal.feature.setting.dto.request.GetAllMenuRequestDto;
import com.internal.feature.setting.dto.request.MenuCreateRequestDto;
import com.internal.feature.setting.dto.request.MenuUpdateRequestDto;
import com.internal.feature.setting.dto.response.AllMenuResponseDto;
import com.internal.feature.setting.dto.response.MenuResponseDto;

import java.util.List;

public interface MenuService {
    
    List<MenuResponseDto> getMenusByCurrentUser();
    
    List<MenuResponseDto> getMenusByUserId(Long userId);
    
    AllMenuResponseDto getAllMenus(GetAllMenuRequestDto request);
    
    MenuResponseDto getMenuById(Long id);
    
    MenuResponseDto createMenu(MenuCreateRequestDto request);
    
    MenuResponseDto updateMenu(Long id, MenuUpdateRequestDto request);
    
    MenuResponseDto deleteMenu(Long id);
    
    MenuResponseDto assignMenuToUsers(Long menuId, AssignMenuToUserRequestDto request);
    
    MenuResponseDto removeMenuFromUsers(Long menuId, List<Long> userIds);

    List<MenuResponseDto> assignMenusToUser(AssignUserMenusRequestDto request);
}
