package com.internal.utils.constants;

public final class ResponseMessage {

    private ResponseMessage() {
    }

    // ===== Generic CRUD Patterns =====
    private static final String RETRIEVED_TEMPLATE = "%s retrieved successfully";
    private static final String CREATED_TEMPLATE = "%s created successfully";
    private static final String UPDATED_TEMPLATE = "%s updated successfully";
    private static final String DELETED_TEMPLATE = "%s deleted successfully";

    public static String retrieved(String entity) {
        return String.format(RETRIEVED_TEMPLATE, entity);
    }

    public static String created(String entity) {
        return String.format(CREATED_TEMPLATE, entity);
    }

    public static String updated(String entity) {
        return String.format(UPDATED_TEMPLATE, entity);
    }

    public static String deleted(String entity) {
        return String.format(DELETED_TEMPLATE, entity);
    }

    // ===== Auth =====
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Log out successful";
    public static final String TOKEN_REFRESHED = "Token refreshed successfully";
    public static final String TOKEN_VALID = "Token is valid";
    public static final String TOKEN_INVALID = "Token is invalid or user account is inactive";
    public static final String ROLES_RETRIEVED = "Available roles retrieved successfully";
    public static final String PROFILE_UPDATED = "User profile updated successfully";

    // ===== User =====
    public static final String USERS_RETRIEVED = "Users retrieved successfully";
    public static final String USER_DETAIL_RETRIEVED = "User details retrieved successfully";
    public static final String CURRENT_USER_RETRIEVED = "Current user details retrieved successfully";
    public static final String USER_CREATED = "User created successfully";
    public static final String USER_UPDATED = "User updated successfully";
    public static final String USER_DELETED = "User deleted successfully";
    public static final String PASSWORD_CHANGED = "Password changed successfully";
    public static final String PASSWORD_CHANGED_BY_ADMIN = "Password changed by admin successfully";

    // ===== API Key =====
    public static final String API_KEY_CREATED = "API Key created successfully";
    public static final String INVALID_API_KEY = "Invalid API Key or Secret Key";

    // ===== AML =====
    public static final String AML_HISTORY_RETRIEVED = "All AML history retrieved successfully";
    public static final String AML_STATUSES_RETRIEVED = "All AML statuses retrieved successfully";
    public static final String AML_STATUS_RETRIEVED = "AML status retrieved successfully";
    public static final String AML_HISTORY_BY_ID_RETRIEVED = "AML history retrieved successfully";
    public static final String AML_STATUS_UPDATED = "AML status updated successfully";
    public static final String AML_EXTERNAL_UPDATED = "AML Risk Level Updated to Low";

    // ===== CAMDX / NID =====
    public static final String NID_VALIDATED = "NID validated successfully";
    public static final String NID_EXTRACTED = "NID extracted successfully";

    // ===== OTP =====
    public static final String OTP_SENT = "OTP sent successfully";
    public static final String OTP_VERIFIED = "OTP verified successfully";

    // ===== Open Account =====
    public static final String ACCOUNT_RETRIEVED = "Account retrieved successfully";
    public static final String SUCCESS_ACCOUNTS_RETRIEVED = "Success accounts retrieved successfully";

    // ===== Report =====
    public static final String REPORT_LOGS_RETRIEVED = "Account online report logs retrieved successfully";

    // ===== Master Data - Branch =====
    public static final String BRANCH = "Branch";
    public static final String ALL_BRANCHES = "All branches";

    // ===== Master Data - Province =====
    public static final String PROVINCE = "Province";
    public static final String ALL_PROVINCES = "All provinces";

    // ===== Master Data - District =====
    public static final String DISTRICT = "District";
    public static final String ALL_DISTRICTS = "All districts";

    // ===== Master Data - Commune =====
    public static final String COMMUNE = "Commune";
    public static final String ALL_COMMUNES = "All communes";

    // ===== Master Data - Village =====
    public static final String VILLAGE = "Village";
    public static final String ALL_VILLAGES = "All villages";

    // ===== Master Data - Legal Type =====
    public static final String LEGAL_TYPE = "Legal type";
    public static final String ALL_LEGAL_TYPES = "All legal types";

    // ===== Master Data - Marital Status =====
    public static final String MARITAL_STATUS = "Marital status";
    public static final String ALL_MARITAL_STATUSES = "All marital statuses";

    // ===== Master Data - Occupation =====
    public static final String OCCUPATION = "Occupation";
    public static final String ALL_OCCUPATIONS = "All occupations";

    // ===== Master Data - Reference (Bank) =====
    public static final String BANK = "Bank";
    public static final String ALL_BANKS = "All banks";

    // ===== Master Data - Public =====
    public static final String ADDRESS_INIT = "Address initialized successfully";
    public static final String POB_INIT = "Place of birth initialized successfully";
    public static final String PROVINCES_RETRIEVED = "Provinces retrieved successfully";
    public static final String DISTRICTS_RETRIEVED = "Districts retrieved successfully";
    public static final String COMMUNES_RETRIEVED = "Communes retrieved successfully";
    public static final String VILLAGES_RETRIEVED = "Villages retrieved successfully";
    public static final String BRANCHES_RETRIEVED = "Branches retrieved successfully";

    // ===== Menu =====
    public static final String MENU = "Menu";
    public static final String ALL_MENUS = "All menus";
    public static final String MENUS_RETRIEVED = "Menus retrieved successfully";
    public static final String USER_MENUS_RETRIEVED = "User menus retrieved successfully";
    public static final String USERS_ASSIGNED_TO_MENU = "Users assigned to menu successfully";
    public static final String USERS_REMOVED_FROM_MENU = "Users removed from menu successfully";
    public static final String MENUS_ASSIGNED_TO_USER = "Menus assigned to user successfully";

    // ===== Image =====
    public static final String IMAGE_UPLOADED = "Image uploaded successfully";
    public static final String IMAGE_DELETED = "Image deleted successfully";
}
