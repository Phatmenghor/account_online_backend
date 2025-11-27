package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "master_legal_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalType extends BaseEntity {

    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;

    @Column(name = "name_kh", nullable = false, length = 50)
    private String nameKh;

    @Column(name = "legal_type_value", length = 50)
    private String legalTypeValue;

    @Column(name = "status", nullable = false)
    private StatusData status;
}
