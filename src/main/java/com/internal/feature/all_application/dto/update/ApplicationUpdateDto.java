package com.internal.feature.all_application.dto.update;

import com.internal.enumation.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateDto {
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
}
