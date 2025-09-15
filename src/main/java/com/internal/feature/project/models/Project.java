package com.internal.feature.project.models;

import com.internal.feature.auth.models.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "host_server")
    private String hostServer;

    @Column(name = "member_involved")
    private String memberInvolved;

    @Column(name = "host_port")
    private Integer hostPort;
    
    @Column(name = "db_name")
    private String dbName;
    
    @Column(name = "db_type")
    private String dbType;
    
    @Column(name = "db_server")
    private String dbServer;
    
    @Column(name = "remark", length = 500)
    private String remark;
}