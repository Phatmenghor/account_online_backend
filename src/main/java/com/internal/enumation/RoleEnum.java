package com.internal.enumation;

import lombok.Getter;

/**
 * Enumeration of system roles with additional information.
 */
@Getter
public enum RoleEnum {
    BUSINESS("Business", "Technical access for all full system"),
    COMPLIANCE("Compliance", "Access to audit, aml, reporting, and regulatory compliance features"),
    DEVELOPER("Developer", "Full system access"),
    ADMIN("Admin", "Full system access");

    /**
     * -- GETTER --
     * Get the human-readable display name of the role.
     *
     * @return Role display name
     */
    private final String displayName;
    /**
     * -- GETTER --
     * Get the description of permissions associated with this role.
     *
     * @return Role description
     */
    private final String description;

    RoleEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the role name as string.
     * 
     * @return Role name
     */
    public String getValue() {
        return this.name();
    }

    /**
     * Checks if the role name matches the enum value (case-insensitive).
     * 
     * @param roleName The role name to check
     * @return true if matches, false otherwise
     */
    public static boolean contains(String roleName) {
        if (roleName == null) {
            return false;
        }

        try {
            RoleEnum.valueOf(roleName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get RoleEnum from string value (case-insensitive).
     * 
     * @param roleName The role name
     * @return RoleEnum value or null if not found
     */
    public static RoleEnum fromString(String roleName) {
        if (roleName == null) {
            return null;
        }

        try {
            return RoleEnum.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
