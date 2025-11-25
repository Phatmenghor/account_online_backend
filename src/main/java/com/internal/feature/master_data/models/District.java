package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "location_district_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class District extends BaseEntity {

    @Column(name = "district_code", nullable = false, length = 50)
    private String districtCode;

    @Column(name = "district_en", length = 255)
    private String districtEn;

    @Column(name = "district_kh", length = 255)
    private String districtKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;
}
