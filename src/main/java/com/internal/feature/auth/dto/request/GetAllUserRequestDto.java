package com.internal.feature.auth.dto.request;

import com.internal.enumation.StatusData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetAllUserRequestDto {

    @Schema(example = "1", defaultValue = "1")
    private int pageNo = 1;

    @Schema(example = "10", defaultValue = "10")
    private int pageSize = 10;

    private String search;
    private StatusData status = StatusData.ACTIVE;
    private java.util.List<String> roles;
}
