package com.internal.feature.telegram_alerts.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nid_validation_cache")
@Data
public class NidValidationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String application_name;
    private String id_number;
    private String first_name_en;
    private String last_name_en;
    private String first_name_kh;
    private String last_name_kh;
    private String dob;
    private String gender;
    private String original_request;
    private String original_response;
    private LocalDateTime created_at;
}
