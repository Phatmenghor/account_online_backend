package com.internal.utils.constants;

public final class AppConstants {

    // ==================================================================================
    // 1. ENVIRONMENT & CONFIGURATION
    // Used in: OtpGenerator, etc.
    // ==================================================================================
    public static final String ENV_DEVELOPMENT = "development";
    public static final String ENV_PRODUCTION = "production";
    public static final String HIGH_RISK = "High";

    public static final String DEFAULT_DEV_OTP = "123456";
    public static final int DEFAULT_OTP_LENGTH = 6;
    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCKOUT_MINUTES = 5;

    // ==================================================================================
    // 2. SUPPORT CONTACT INFORMATION
    // Used in: Various services to append support info
    // ==================================================================================
    public static final String SUPPORT_PHONE_PRIMARY = "070 200 002";
    public static final String SUPPORT_PHONE_SECONDARY = "1800 200 888";
    
    public static final String SUPPORT_CONTACT = "សូមទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ " + SUPPORT_PHONE_PRIMARY + " ឬ " + SUPPORT_PHONE_SECONDARY + " ដើម្បីទទួលបានជំនួយបន្ថែម។";

    // ==================================================================================
    // 3. NID VALIDATION ERRORS (Generic)
    // Used in: CamdxServiceImp, internal validations
    // ==================================================================================
    public static final String NID_ERROR_400 = "ព័ត៌មានអត្តសញ្ញាណប័ណ្ណមិនត្រឹមត្រូវ។ សូមពិនិត្យមើលលេខអត្តសញ្ញាណប័ណ្ណជាតិរបស់អ្នក។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_401 = "ការផ្ទៀងផ្ទាត់មិនបានជោគជ័យ។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_403 = "ការចូលប្រើត្រូវបានបដិសេធ។ អ្នកមិនមានសិទ្ធិក្នុងការផ្ទៀងផ្ទាត់អត្តសញ្ញាណប័ណ្ណនេះទេ។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_404 = "រកមិនឃើញសេវាកម្មផ្ទៀងផ្ទាត់អត្តសញ្ញាណប័ណ្ណទេ។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_408 = "ការស្នើសុំលើសម៉ោងកំណត់។ សូមព្យាយាមម្តងទៀត។ ប្រសិនបើបញ្ហានៅតែបន្ត សូមទំនាក់ទំនងសេវាបម្រើអតិថិជន។";
    public static final String NID_ERROR_429 = "ការព្យាយាមផ្ទៀងផ្ទាត់ច្រើនដងពេក។ សូមរង់ចាំហើយព្យាយាមម្តងទៀត។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_500 = "មានបញ្ហាសេវាកម្ម។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ ឬទំនាក់ទំនងសេវាបម្រើអតិថិជន។";
    public static final String NID_ERROR_502_503 = "សេវាកម្មមិនដំណើរការជាបណ្តោះអាសន្ន។ សូមព្យាយាមម្តងទៀតក្នុងរយៈពេលពីរបីនាទី។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_504 = "ការស្នើសុំលើសម៉ោងកំណត់។ សូមព្យាយាមម្តងទៀត។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_DEFAULT = "មិនអាចផ្ទៀងផ្ទាត់អត្តសញ្ញាណប័ណ្ណនៅពេលនេះបានទេ។ " + SUPPORT_CONTACT;
    public static final String NID_ERROR_SYSTEM = "មានបញ្ហាបច្ចេកទេស។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ ឬទំនាក់ទំនងសេវាបម្រើអតិថិជន។";

    // ==================================================================================
    // 4. CAMDX SPECIFIC ERRORS (MSG_*)
    // Used in: CamdxServiceImp
    // ==================================================================================
    public static final String MSG_400 = "អត្តសញ្ញាណប័ណ្ណមិនត្រូវបានទទួលស្គាល់ដោយប្រព័ន្ធរបស់យើងទេ។ សូមទាក់ទងផ្នែកគ្រប់គ្រងសម្រាប់ដំណោះស្រាយផ្សេងទៀត។";
    public static final String MSG_420 = "ការស្នើសុំលើសចំនួនកំណត់។ សូមទាក់ទងក្រុមបច្ចេកទេសដើម្បីទទួលបានជំនួយ។";
    public static final String MSG_500 = "រកមិនឃើញមុខនៅក្នុងរូបថតរបស់អ្នកទេ។ សូមថតរូបអោយច្បាស់ហើយព្យាយាមម្តងទៀត។";
    public static final String MSG_501 = "មិនអាចចាប់យកផ្ទៃមុខនៅលើអត្តសញ្ញាណប័ណ្ណបានទេ។ សូមបញ្ចូលរូបភាពអត្តសញ្ញាណប័ណ្ណរបស់អ្នកអោយបានច្បាស់។";
    public static final String MSG_502 = "មានបញ្ហាបច្ចេកទេស។ សូមពិនិត្យមើលអត្តសញ្ញាណប័ណ្ណរបស់អ្នក ហើយព្យាយាមម្តងទៀតក្នុងរយៈពេលពីរបីនាទី ឬទំនាក់ទំនងសេវាបម្រើអតិថិជន។";
    public static final String MSG_503 = "បញ្ហានៃការតភ្ជាប់ជាមួយសេវាកម្មផ្ទៀងផ្ទាត់។ សូមព្យាយាមម្តងទៀត។";
    public static final String MSG_504 = "ការផ្ទៀងផ្ទាត់អត្តសញ្ញាណប័ណ្ណជាមួយ CAMDX បរាជ័យ។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ។";

    // ==================================================================================
    // 5. ACCOUNT CREATION & OPEN ACCOUNT ERRORS
    // Used in: OpenAccountServiceImpl, OpenAcctController (legacy logic)
    // ==================================================================================
    public static final String ACCOUNT_ALREADY_EXIST = "ACCOUNT_ALREADY_EXIST"; // ID
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR"; // ID
    public static final String ACCOUNT_RISK = "ACCOUNT_RISK"; // ID
    public static final String AML_HIT = "AML_RE"; // ID
    public static final String ACCOUNT_CREATE_FAIL = "ACCOUNT_CREATE_FAIL"; // ID

    public static final String FAIL_CREATE_ANY_ACCOUNT = "មិនអាចបង្កើតគណនីបានទេ។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ។ ប្រសិនបើបញ្ហានៅតែបន្ត សូមទំនាក់ទំនងសេវាបម្រើអតិថិជន។";
    public static final String MSG_DB_CONNECTION_ERR = "មានបញ្ហាក្នុងការតភ្ជាប់ទៅកាន់ប្រព័ន្ធ សូមព្យាយាមម្តងទៀត";
    public static final String MSG_HIGH_RISK_ERR = "សំណើរបស់អ្នកមិនអាចដំណើរការបានទេ ពីព្រោះការវាយតម្លៃអតិថិជនមានការហានិភ័យ";
    public static final String MSG_ACCOUNT_EXISTS_ERR = "លោកអ្នកមានគណនីជាមួយធនាគាររួចហើយ។ សូមប្រើប្រាស់ជាមួយគណនីរបស់លោកអ្នក។";
    public static final String MSG_CREATE_CUSTOMER_ERR = "ការស្នើសុំរបស់លោកអ្នកមិនអាចដំណើរការបានទេ។ សូមព្យាយាមម្តងទៀត";
    public static final String MSG_GENERIC_ERROR = "ការស្នើសុំរបស់លោកអ្នកមិនអាចដំណើរការបានទេ។ សូមព្យាយាមម្តងទៀត"; // For 500, 503, 504, 505, 506
    public static final String MSG_CREATE_ACCOUNT_ERR = "មិនអាចបង្កើតគណនីណាមួយបានទេ។ សូមព្យាយាមម្តងទៀត។";
    public static final String MSG_SUCCESS = "ការស្នើសុំជោគជ័យ។ លោកអ្នកនឹងទទួលបានលេខគណនី តាមសារទូរស័ព្ទ។ សូមភ្ជាប់សេវាធនាគារចល័តដើម្បីរីករាយជាមួយប្រតិបត្តិការដ៏សម្បូរបែប។";

    // ==================================================================================
    // 6. AML MESSAGES
    // Used in: OpenAccountServiceImpl
    // ==================================================================================
    public static final String AML_NEED_REVIEW_MSG = "ការត្រួតពិនិត្យ AML បង្ហាញថាហានិភ័យខ្ពស់ឬកំពុងរង់ចាំសម្រាប់អត្តសញ្ញាណប័ណ្ណលេខ %s។ ពាក្យស្នើសុំនេះត្រូវការការត្រួតពិនិត្យដោយផ្ទាល់។ " + SUPPORT_CONTACT;
    public static final String AML_REJECTED_MSG = "ការត្រួតពិនិត្យ AML ត្រូវបានបដិសេធសម្រាប់អត្តសញ្ញាណប័ណ្ណលេខ %s។ គណនីមិនអាចបង្កើតបានទេ។ " + SUPPORT_CONTACT;
    public static final String AML_UNKNOWN_MSG = "ស្ថានភាព AML មិនស្គាល់សម្រាប់អត្តសញ្ញាណប័ណ្ណលេខ %s។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ ឬទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ " + SUPPORT_PHONE_PRIMARY + " ឬ " + SUPPORT_PHONE_SECONDARY + "។";

    // ==================================================================================
    // 7. OPEN ACCOUNT WORKFLOW STEPS
    // Used in: OpenAccountServiceImpl for tracking progress
    // ==================================================================================
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
    
    // ==================================================================================
    // 8. FIELD NAME TRANSLATIONS
    // Used in: CamdxErrorCheckServiceImpl
    // ==================================================================================
    public static final String FIELD_KH_LASTNAME_EN = "នាមត្រកូល (អង់គ្លេស)";
    public static final String FIELD_KH_FIRSTNAME_EN = "នាមខ្លួន (អង់គ្លេស)";
    public static final String FIELD_KH_DOB = "ថ្ងៃខែឆ្នាំកំណើត";
    public static final String FIELD_KH_GENDER = "ភេទ";

    private AppConstants() {
        // Prevent instantiation
    }
}
