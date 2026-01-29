package com.internal.utils.constants;

public final class MasterDataConstants {

    // ===== Sorting =====
    public static final String SORT_CREATED_AT = "createdAt";

    // ===== Address Prefixes (Khmer) =====
    public static final String PREFIX_PROVINCE = ""; // province has no prefix
    public static final String PREFIX_DISTRICT_1 = "ស្រុក";
    public static final String PREFIX_DISTRICT_2 = "ក្រុង";
    public static final String PREFIX_DISTRICT_3 = "ខណ្ឌ";

    public static final String PREFIX_COMMUNE_1 = "ឃុំ";
    public static final String PREFIX_COMMUNE_2 = "សង្កាត់";

    public static final String PREFIX_VILLAGE = "ភូមិ";

    // ===== Entity Fields =====
    public static final String FIELD_PROVINCE = "province";
    public static final String FIELD_DISTRICT = "district";
    public static final String FIELD_COMMUNE = "commune";

    public static final String FIELD_PROVINCE_CODE = "provinceCode";
    public static final String FIELD_DISTRICT_CODE = "districtCode";
    public static final String FIELD_COMMUNE_CODE = "communeCode";

    // ===== Branch fields =====
    public static final String FIELD_BRANCH_CODE = "branchCode";
    public static final String FIELD_BRANCH_KH = "branchKh";

    private MasterDataConstants() {
    }
}
