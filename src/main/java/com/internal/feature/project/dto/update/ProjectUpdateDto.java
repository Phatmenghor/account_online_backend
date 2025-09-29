package com.internal.feature.project.dto.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateDto {
    private String projectName;
    private String type;
    private String hostServer;
    private Integer hostPort;
    private String dbName;
    private String memberInvolved;
    private String dbType;
    private String dbServer;
    private String remark;
}
