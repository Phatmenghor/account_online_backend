package com.internal.feature.auth.models;

import com.internal.config.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_api_keys")
@Data
@NoArgsConstructor
public class ApiKeyEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String secretKeyHash;

    private String clientName;

    private boolean isActive = true;
}
