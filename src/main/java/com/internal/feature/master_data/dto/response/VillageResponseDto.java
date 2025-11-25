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
public class VillageResponseDto {
    private Long id;
    private String villageCode;
    private String villageEn;
    private String villageKh;
    private CommuneResponseDto commune;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
