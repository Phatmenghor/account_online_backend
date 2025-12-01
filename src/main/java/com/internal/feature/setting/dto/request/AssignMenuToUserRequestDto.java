package com.internal.feature.setting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignMenuToUserRequestDto {
    
    @NotEmpty(message = "User IDs are required")
    private List<Long> userIds;
}
