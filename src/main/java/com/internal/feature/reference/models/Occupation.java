package com.internal.feature.reference.models;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "acc_online_occupation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Occupation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_kh", nullable = false, length = 100)
    private String nameKh;

    @Column(name = "occupation_code", nullable = true)
    private String occupationCode;

    @Column(name = "status", nullable = false)
    private StatusData status;
}