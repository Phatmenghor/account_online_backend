package com.internal.feature.master_data.models;

import com.internal.config.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "acc_online_branch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseEntity {

    @Column(name = "branch_code", nullable = false, length = 50)
    private String branchCode;

    @Column(name = "branch_kh", length = 255)
    private String branchKh;
    

}
