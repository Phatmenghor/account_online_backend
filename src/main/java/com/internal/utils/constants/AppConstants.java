package com.internal.utils.constants;

public final class AppConstants {

    public static final String ENV_DEVELOPMENT = "development";
    public static final String ENV_PRODUCTION = "production";
    public static final String HIGH_RISK = "High";

    public static final String DEFAULT_DEV_OTP = "123456";
    public static final int DEFAULT_OTP_LENGTH = 6;
    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCKOUT_MINUTES = 5;

    // Support contact information
    public static final String SUPPORT_CONTACT = "Please contact support at 070 200 002 or 1800 200 888 for further assistance.";
    public static final String SUPPORT_PHONE_PRIMARY = "070 200 002";
    public static final String SUPPORT_PHONE_SECONDARY = "1800 200 888";

    // NID Validation Error Messages
    public static final String NID_ERROR_400 = "Invalid NID information. Please verify your National ID number. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_401 = "Authentication failed. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_403 = "Access denied. You don't have permission to validate this NID. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_404 = "NID validation service not found. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_408 = "Request timeout. Please try again in a moment. If the issue persists, contact support at "
            + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";
    public static final String NID_ERROR_429 = "Too many validation attempts. Please wait and try again. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_500 = "Service error occurred. Please try again later or contact support at "
            + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";
    public static final String NID_ERROR_502_503 = "Service temporarily unavailable. Please try again in a few minutes. If the problem continues, contact support at "
            + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";
    public static final String NID_ERROR_504 = "Request timeout. Please try again in a moment. If the issue persists, contact support at "
            + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";
    public static final String NID_ERROR_DEFAULT = "Unable to validate NID at this time. " + SUPPORT_CONTACT;
    public static final String NID_ERROR_SYSTEM = "System error occurred. Please try again later or contact support at "
            + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";

    // áŸ„áž€áž¢áŸ’áž“áž€áž˜áž¶áž“áž‚ážŽáž“áž¸áž‡áž¶áž˜áž½áž™áž’áž“áž¶áž‚áž¶ážšážšáž½áž…áž áž¾áž™áŸ” ážŸáž¼áž˜áž”áŸ’ážšáž¾áž”áŸ’ážšáž¶ážŸáŸ‹áž‡áž¶áž˜áž½áž™áž‚ážŽáž“áž¸ážšáž”ážŸáŸ‹áž›áŸ„áž€áž¢áŸ’áž“áž€áŸ”
    public static final String ACCOUNT_ALREADY_EXIST = "ACCOUNT_ALREADY_EXIST";
    // áž˜áž¶áž“áž”áž‰áŸ’áž áž¶áž€áŸ’áž“áž»áž„áž€áž¶ážšážáž—áŸ’áž‡áž¶áž”áŸ‹áž‘áŸ…áž€áž¶áž“áŸ‹áž”áŸ’ážšáž–áŸáž“áŸ’áž’ ážŸáž¼áž˜áž–áŸ’áž™áž¶áž™áž¶áž˜áž˜áŸ’ážáž„áž‘áŸ€áž
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    // ážŸáŸ†ážŽáž¾ážšáž”ážŸáŸ‹áž¢áŸ’áž“áž€áž˜áž·áž“áž¢áž¶áž…ážŠáŸ†ážŽáž¾ážšáž€áž¶ážšáž”áž¶áž“áž‘áŸ áž–áž¸áž–áŸ’ážšáŸ„áŸ‡áž€áž¶ážšážœáž¶áž™ážáž˜áŸ’áž›áŸƒáž¢ážáž·ážáž·áž‡áž“áž˜áž¶áž“áž€áž¶ážšáž áž¶áž“áž·áž—áŸáž™
    public static final String ACCOUNT_RISK = "ACCOUNT_RISK";
    public static final String AML_HIT = "AML_RE";
    // áž€áž¶ážšážŸáŸ’áž“áž¾ážŸáž»áŸ†ážšáž”ážŸáŸ‹áž›áŸ„áž€áž¢áŸ’áž“áž€áž˜áž·áž“áž¢áž¶áž…ážŠáŸ†ážŽáž¾ážšáž€áž¶ážšáž”áž¶áž“áž‘áŸáŸ” ážŸáž¼áž˜áž–áŸ’áž™áž¶áž™áž¶áž˜áž˜áŸ’ážáž„áž‘áŸ€áž
    public static final String ACCOUNT_CREATE_FAIL = "ACCOUNT_CREATE_FAIL";
    public static final String FAIL_CREATE_ANY_ACCOUNT =
            "Unable to create the account. Please try again later. If the issue continues, contact support at "
                    + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";

    // AML messages with support contacts
    public static final String AML_NEED_REVIEW_MSG =
            "AML check indicates HIGH RISK or PENDING for Legal ID %s. This application requires manual review. " +
                    SUPPORT_CONTACT;

    public static final String AML_REJECTED_MSG =
            "AML check REJECTED for Legal ID %s. The account cannot be created. " +
                    SUPPORT_CONTACT;

    public static final String AML_UNKNOWN_MSG =
            "AML status is UNKNOWN for Legal ID %s. Please try again later or " +
                    "contact support at " + SUPPORT_PHONE_PRIMARY + " or " + SUPPORT_PHONE_SECONDARY + ".";

    //OPEN ACCOUNT STEPS
    public static final String TEST_CONNECTION = "TEST_CONNECTION";
    public static final String PROCESS_AML = "PROCESS_AML";
    public static final String GET_CUSTOMER_INFO = "GET_CUSTOMER_INFO";
    public static final String CREATE_CUSTOMER = "CREATE_CUSTOMER";
    public static final String CREATE_KHR_ACCOUNT = "CREATE_KHR_ACCOUNT";
    public static final String CREATE_USD_ACCOUNT = "CREATE_USD_ACCOUNT";
    public static final String VALIDATE_ACCOUNT_CREATION = "VALIDATE_ACCOUNT_CREATION";
    public static final String ACTIVATE_MOBILE_BANKING = "ACTIVATE_MOBILE_BANKING";
    public static final String SAVE_CUSTOMER_IMAGES = "SAVE_CUSTOMER_IMAGES";
    public static final String SAVE_FINAL_LOG = "SAVE_FINAL_LOG";

    private AppConstants() {
        // Prevent instantiation
    }
}
