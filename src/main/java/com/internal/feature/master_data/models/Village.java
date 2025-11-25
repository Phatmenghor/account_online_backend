package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "location_village_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Village extends BaseEntity {

    @Column(name = "village_code", nullable = false, length = 50)
    private String villageCode;

    @Column(name = "village_en", length = 255)
    private String villageEn;

    @Column(name = "village_kh", length = 255)
    private String villageKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;
}
