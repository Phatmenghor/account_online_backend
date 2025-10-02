package com.internal.feature.all_application.models;

import com.internal.enumation.ApplicationStatusEnum;
import com.internal.enumation.ProjectStatusEnum;
import com.internal.feature.auth.models.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application extends BaseEntity {

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "member_involved")
    private String memberInvolved;

    @Lob
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "department")
    private String department;

    @Column(name = "year")
    private String year;

    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum applicationStatus;

    @Column(name = "urlLink")
    private String urlLink;
}