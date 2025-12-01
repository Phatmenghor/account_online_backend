package com.internal.feature.setting.dto.response;

import com.internal.enumation.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDto {
    private Long id;
    private String title;
    private String icon;
    private String href;
    private Long parentId;
    private Integer displayOrder;
    private Set<RoleEnum> roles;
    private Boolean isActive;
}
