
package com.internal.feature.open_account.models;

import com.internal.config.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tbl_sms_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cif")
    private String cif;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "acct_id_khr")
    private String acctIdKhr;
    
    @Column(name = "acct_id_usd")
    private String acctIdUsd;
    
    @Column(name = "nid")
    private String nid;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "activator_payload", columnDefinition = "TEXT")
    private String activatorPayload;
    
    @Column(name = "activator_response", columnDefinition = "TEXT")
    private String activatorResponse;
}