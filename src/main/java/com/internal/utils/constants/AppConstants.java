package com.internal.utils.constants;

public final class AppConstants {

    public static final String ENV_DEVELOPMENT = "development";
    public static final String ENV_PRODUCTION = "production";

    public static final String DEFAULT_DEV_OTP = "123456";
    public static final int DEFAULT_OTP_LENGTH = 6;
    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCKOUT_MINUTES = 5;

    // ោកអ្នកមានគណនីជាមួយធនាគាររួចហើយ។ សូមប្រើប្រាស់ជាមួយគណនីរបស់លោកអ្នក។
    public static final String ACCOUNT_ALREADY_EXIST = "ACCOUNT_ALREADY_EXIST";
    // មានបញ្ហាក្នុងការតភ្ជាប់ទៅកាន់ប្រព័ន្ធ សូមព្យាយាមម្តងទៀត
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    // សំណើរបស់អ្នកមិនអាចដំណើរការបានទេ ពីព្រោះការវាយតម្លៃអតិថិជនមានការហានិភ័យ
    public static final String ACCOUNT_RISK = "ACCOUNT_RISK";
    // ការស្នើសុំរបស់លោកអ្នកមិនអាចដំណើរការបានទេ។ សូមព្យាយាមម្តងទៀត
    public static final String ACCOUNT_CREATE_FAIL = "ACCOUNT_CREATE_FAIL";
    public static final String FAIL_CREATE_ANY_ACCOUNT = "FAIL_CREATE_ANY_ACCOUNT";

    // AML messages
    public static final String AML_NEED_REVIEW_MSG = "AML check indicates HIGH RISK or PENDING. Legal ID %s needs review.";
    public static final String AML_REJECTED_MSG    = "AML check REJECTED for Legal ID %s. Account cannot be created.";
    public static final String AML_UNKNOWN_MSG     = "AML UNKNOWN status for Legal ID %s.";
}