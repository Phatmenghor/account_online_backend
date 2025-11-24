package com.internal.feature.master_data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "location_village_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Village implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "village_code", nullable = false, length = 50)
    private String villageCode;

    @Column(name = "village_en", length = 255)
    private String villageEn;

    @Column(name = "village_kh", length = 255)
    private String villageKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_code", referencedColumnName = "commune_code")
    private Commune commune;
}
