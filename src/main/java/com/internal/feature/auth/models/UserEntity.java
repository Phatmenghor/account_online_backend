package com.internal.feature.auth.models;

import com.internal.enumation.StatusData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username; // idCard

    private String email;

    private String password;

    private String fullName;

    private String position;

    private String branch;

    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private StatusData status;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();
}