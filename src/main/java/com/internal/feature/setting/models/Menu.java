package com.internal.feature.setting.models;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.RoleEnum;
import com.internal.feature.auth.models.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String icon;

    @Column(length = 500)
    private String href;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Menu> children = new ArrayList<>();

    @Column(nullable = false)
    private Integer displayOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "acc_online_menu_roles", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<RoleEnum> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "acc_online_menu_users",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserEntity> allowedUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "acc_online_menu_excluded_users",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserEntity> excludedUsers = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void addChild(Menu child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Menu child) {
        children.remove(child);
        child.setParent(null);
    }
}
