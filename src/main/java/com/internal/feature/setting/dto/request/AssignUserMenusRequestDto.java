package com.internal.feature.setting.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssignUserMenusRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Menu IDs list is required")
    private List<Long> menuIds;
}
