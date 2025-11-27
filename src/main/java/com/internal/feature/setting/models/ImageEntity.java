package com.internal.feature.setting.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "acc_online_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "type")
    private String type;
    
    @Lob
    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // Base64 encoded string
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
