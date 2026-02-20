package com.internal.feature.setting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllMenuRequestDto {

    @Schema(example = "1", defaultValue = "1")
    private int pageNo = 1;

    @Schema(example = "10", defaultValue = "10")
    private int pageSize = 10;

    private String search;

    private Boolean isActive;

    private Long parentId;
}
