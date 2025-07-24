package com.account_sell.feature.auth.models;

import com.account_sell.enumation.ActionEnum;
import com.account_sell.enumation.StatusLogEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "log_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAction extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionEnum actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_log")
    private StatusLogEnum statusLog;

    private String statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}