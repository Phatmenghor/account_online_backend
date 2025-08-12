package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_id_information")
public class CbcIdInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    // ID Set 1
    @Column(name = "id_type_1")
    private String idType1;
    
    @Column(name = "id_number_1")
    private String idNumber1;
    
    @Column(name = "id_expiry_date_1")
    private String idExpiryDate1;
    
    // ID Set 2
    @Column(name = "id_type_2")
    private String idType2;
    
    @Column(name = "id_number_2")
    private String idNumber2;
    
    @Column(name = "id_expiry_date_2")
    private String idExpiryDate2;
    
    // ID Set 3
    @Column(name = "id_type_3")
    private String idType3;
    
    @Column(name = "id_number_3")
    private String idNumber3;
    
    @Column(name = "id_expiry_date_3")
    private String idExpiryDate3;
}