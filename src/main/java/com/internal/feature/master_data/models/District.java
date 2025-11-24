package com.internal.feature.master_data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "location_district_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class District {
    @Id
    @Column(name = "district_code", nullable = false, length = 50)
    private String districtCode;

    @Column(name = "district_en", length = 255)
    private String districtEn;

    @Column(name = "district_kh", length = 255)
    private String districtKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "province_code")
    private Province province;
}
