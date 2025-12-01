package com.internal.feature.setting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllMenuRequestDto {
    
    @Builder.Default
    private int pageNo = 1;
    
    @Builder.Default
    private int pageSize = 10;
    
    private String search;
    
    private Boolean isActive;
    
    private Long parentId;
}
