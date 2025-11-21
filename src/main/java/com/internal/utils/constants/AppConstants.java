package com.internal.utils.constants;

public final class AppConstants {

    public static final String ENV_DEVELOPMENT = "development";
    public static final String ENV_PRODUCTION = "production";
    public static final String HIGH_RISK = "High";

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
    public static final String AML_HIT = "AML_RE";
    // ការស្នើសុំរបស់លោកអ្នកមិនអាចដំណើរការបានទេ។ សូមព្យាយាមម្តងទៀត
    public static final String ACCOUNT_CREATE_FAIL = "ACCOUNT_CREATE_FAIL";
    public static final String FAIL_CREATE_ANY_ACCOUNT =
            "Unable to create the account. Please try again later. If the issue continues, contact support at 070 200 002 or 1800 200 888.";

    // AML messages with support contacts
    public static final String AML_NEED_REVIEW_MSG =
            "AML check indicates HIGH RISK or PENDING for Legal ID %s. This application requires manual review. " +
                    "For assistance, please contact support at 070 200 002 or 1800 200 888.";

    public static final String AML_REJECTED_MSG =
            "AML check REJECTED for Legal ID %s. The account cannot be created. " +
                    "Please contact support at 070 200 002 or 1800 200 888 for further assistance.";

    public static final String AML_UNKNOWN_MSG =
            "AML status is UNKNOWN for Legal ID %s. Please try again later or contact support at 070 200 002 or 1800 200 888.";


    //OPEN ACCOUNT STEPS
    public static final String TEST_CONNECTION = "TEST_CONNECTION";
    public static final String PROCESS_AML = "PROCESS_AML";
    public static final String GET_CUSTOMER_INFO = "GET_CUSTOMER_INFO";
    public static final String CREATE_CUSTOMER = "CREATE_CUSTOMER";
    public static final String CREATE_KHR_ACCOUNT = "CREATE_KHR_ACCOUNT";
    public static final String CREATE_USD_ACCOUNT = "CREATE_USD_ACCOUNT";
    public static final String VALIDATE_ACCOUNT_CREATION = "VALIDATE_ACCOUNT_CREATION";
    public static final String ACTIVATE_MOBILE_BANKING = "ACTIVATE_MOBILE_BANKING";
    public static final String UPDATE_AML_WITH_ACCOUNTS = "UPDATE_AML_WITH_ACCOUNTS";
    public static final String SAVE_CUSTOMER_IMAGES = "SAVE_CUSTOMER_IMAGES";
    public static final String SAVE_FINAL_LOG = "SAVE_FINAL_LOG";
}