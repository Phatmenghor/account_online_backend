package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "master_marital_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaritalStatus extends BaseEntity {

    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;

    @Column(name = "name_kh", nullable = false, length = 50)
    private String nameKh;

    @Column(name = "status", nullable = false)
    private StatusData status;
}
