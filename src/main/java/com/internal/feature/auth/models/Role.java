package com.internal.feature.auth.models;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.RoleEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "acc_online_roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleEnum name;
}
