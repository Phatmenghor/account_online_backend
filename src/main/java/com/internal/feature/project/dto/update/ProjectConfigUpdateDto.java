package com.internal.feature.project.dto.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectConfigUpdateDto {
    
    private String projectName;
    private String type;
    private String hostServer;
    
    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than 65536")
    private Integer hostPort;
    
    private String dbName;
    private String dbType;
    private String dbServer;
    private String remark;
}