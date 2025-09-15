package com.internal.feature.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDto {
    private String projectName;
    private String type;
    private String hostServer;
    private Integer hostPort;
    private String dbName;
    private String dbType;
    private String dbServer;
    private String remark;
}