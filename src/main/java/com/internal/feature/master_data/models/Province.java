package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "location_province_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Province extends BaseEntity {

    @Column(name = "province_code", nullable = false, length = 50)
    private String provinceCode;

    @Column(name = "province_en", length = 255)
    private String provinceEn;

    @Column(name = "province_kh", length = 255)
    private String provinceKh;
}
