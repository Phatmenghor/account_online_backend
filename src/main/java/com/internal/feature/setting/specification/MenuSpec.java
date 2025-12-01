package com.internal.feature.setting.specification;

import com.internal.enumation.RoleEnum;
import com.internal.feature.setting.models.Menu;
import lombok.var;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;

public class MenuSpec {

    public static Specification<Menu> searchByTitle(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), searchPattern);
        };
    }

    public static Specification<Menu> hasStatus(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<Menu> hasParentId(Long parentId) {
        return (root, query, cb) -> {
            if (parentId == null) {
                return cb.conjunction();
            }
            if (parentId == 0L) {
                // Root menus (no parent)
                return cb.isNull(root.get("parent"));
            }
            return cb.equal(root.get("parent").get("id"), parentId);
        };
    }

    public static Specification<Menu> hasRole(RoleEnum role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.isMember(role, root.get("roles"));
        };
    }

    public static Specification<Menu> hasUserAccess(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            var userJoin = root.join("allowedUsers", JoinType.LEFT);
            return cb.equal(userJoin.get("id"), userId);
        };
    }
}
