package com.internal.feature.master_data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "location_commune_cbc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commune implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commune_code", nullable = false, length = 50)
    private String communeCode;

    @Column(name = "commune_en", length = 255)
    private String communeEn;

    @Column(name = "commune_kh", length = 255)
    private String communeKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code", referencedColumnName = "district_code")
    private District district;
}
