package com.internal.feature.setting.dto.request;

import com.internal.enumation.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCreateRequestDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String icon;
    
    private String href;
    
    // Option 1: Use existing parent menu by ID
    private Long parentId;
    
    // Option 2: Create new parent menu
    @Valid
    private ParentMenuDto parentMenu;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    private Set<RoleEnum> roles;
    
    private List<Long> allowedUserIds;
    
    @Builder.Default
    private Boolean isActive = true;
    
    // Validation: Cannot provide both parentId and parentMenu
    @AssertTrue(message = "Cannot provide both parentId and parentMenu. Choose one option only.")
    private boolean isValidParentOption() {
        // Both can be null (root menu), or only one can be provided
        return !(parentId != null && parentMenu != null);
    }
}
