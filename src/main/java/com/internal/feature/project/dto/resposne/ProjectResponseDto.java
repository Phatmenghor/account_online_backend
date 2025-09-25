package com.internal.feature.project.dto.resposne;

import com.internal.enumation.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String projectName;
    private String type;
    private String hostServer;
    private Integer hostPort;
    private String dbName;
    private String memberInvolved;
    private String dbType;
    private String dbServer;
    private String remark;
    private ProjectStatusEnum projectStatus;
    private String gitUrl;
    private String gitBranch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
