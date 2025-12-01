package com.internal.feature.setting.dto.request;

import com.internal.enumation.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private Long parentId;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    private Set<RoleEnum> roles;
    
    private List<Long> allowedUserIds;
    
    @Builder.Default
    private Boolean isActive = true;
}
