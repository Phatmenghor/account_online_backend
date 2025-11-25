package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponseDto {
    private Long id;
    private String branchCode;
    private String branchKh;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
