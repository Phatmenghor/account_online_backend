package com.internal.utils.constants;

public final class AppConstants {

        // ==================================================================================
        // 1. ENVIRONMENT & CONFIGURATION
        // Used in: OtpGenerator, etc.
        // ==================================================================================
        // Risk Levels
        public static final String RISK_HIGH = "High";
        public static final String RISK_LOW = "Low";

        // Currency
        public static final String CURRENCY_KHR = "KHR";
        public static final String CURRENCY_USD = "USD";

        // OTP
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
        public static final String SUPPORT_CONTACT = "មានបញ្ហាបច្ចេកទេស។ សូមទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ "
                        + SUPPORT_PHONE_PRIMARY + " ឬ " + SUPPORT_PHONE_SECONDARY + " ដើម្បីទទួលបានជំនួយបន្ថែម។";

        // ==================================================================================
        // 3. NID VALIDATION ERRORS (Generic)
        // Used in: CamdxServiceImp, internal validations
        // ==================================================================================
        public static final String NID_ERROR_SYSTEM = "មានបញ្ហាបច្ចេកទេស។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ ឬទំនាក់ទំនងសេវាបម្រើអតិថិជន។";

        // ==================================================================================
        // 4. CAMDX SPECIFIC ERRORS (MSG_*)
        // Used in: CamdxServiceImp
        // ==================================================================================
        public static final String MSG_400 = "អត្តសញ្ញាណប័ណ្ណរបស់អ្នកមិនអាចរកឃើញក្នុងប្រពន្ធ័ Ministry of Interior (MOI) បានទេ។ សូមទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ 070 200 002 ឬ 1800 200 888 ដើម្បីទទួលបានជំនួយបន្ថែម។";
        public static final String MSG_420 = "ការស្នើសុំលើសចំនួនកំណត់។ សូមទាក់ទងក្រុមបច្ចេកទេសដើម្បីទទួលបានជំនួយ។";
        public static final String MSG_500 = "រកមិនឃើញមុខនៅក្នុងរូបថតរបស់អ្នកទេ។ សូមថតរូបអោយច្បាស់ហើយព្យាយាមម្តងទៀត។";
        public static final String MSG_501 = "មិនអាចចាប់យកផ្ទៃមុខនៅលើអត្តសញ្ញាណប័ណ្ណបានទេ។ សូមបញ្ចូលរូបភាពអត្តសញ្ញាណប័ណ្ណរបស់អ្នកអោយបានច្បាស់។";
        public static final String MSG_502 = "មានបញ្ហាបច្ចេកទេស។ សូមពិនិត្យមើលអត្តសញ្ញាណប័ណ្ណរបស់អ្នក ហើយព្យាយាមម្តងទៀតក្នុងរយៈពេលពីរបីនាទី ឬទំនាក់ទំនងសេវាបម្រើអតិថិជនជនតាមរយៈលេខ 070 200 002 ឬ 1800 200 888។";
        public static final String MSG_503 = "ការស្នើសុំមិនអាចភ្ជាប់ទៅប្រព័ន្ធយើងខ្ញុំទេ។ សូមពិនិត្យសេវាអ៊ីនធឺណែត រួចព្យាយាមម្តងទៀត។";
        public static final String MSG_504 = "ការផ្ទៀងផ្ទាត់អត្តសញ្ញាណប័ណ្ណជាមួយ CAMDX បរាជ័យ។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ។";

        // ==================================================================================
        // 5. ACCOUNT CREATION & OPEN ACCOUNT ERRORS
        // Used in: OpenAccountServiceImpl, OpenAcctController (legacy logic)
        // ==================================================================================
        public static final String FAIL_CREATE_ANY_ACCOUNT = "មិនអាចបង្កើតគណនីបានទេ។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ។ ប្រសិនបើបញ្ហានៅតែបន្ត សូមទំនាក់ទំនងសេវាបម្រើអតិថិជន។";
        public static final String MSG_DB_CONNECTION_ERR = "មានបញ្ហាក្នុងការតភ្ជាប់ទៅកាន់ប្រព័ន្ធ សូមព្យាយាមម្តងទៀត";
        public static final String MSG_HIGH_RISK_ERR = "សំណើរបស់អ្នកមិនអាចដំណើរការបានទេ ពីព្រោះការវាយតម្លៃអតិថិជនមានការហានិភ័យ";
        public static final String MSG_ACCOUNT_EXISTS_ERR = "លោកអ្នកមានគណនីជាមួយធនាគាររួចហើយ។ សូមប្រើប្រាស់ជាមួយគណនីរបស់លោកអ្នក។";
        public static final String MSG_GENERIC_ERROR = "ការស្នើសុំរបស់លោកអ្នកមិនអាចដំណើរការបានទេ។ សូមព្យាយាមម្តងទៀត"; // For
                                                                                                                      // 500,
                                                                                                                      // 503,
                                                                                                                      // 504,
                                                                                                                      // 505,
                                                                                                                      // 506
        public static final String MSG_SUCCESS = "ការស្នើសុំជោគជ័យ។ លោកអ្នកនឹងទទួលបានលេខគណនី តាមសារទូរស័ព្ទ។ សូមភ្ជាប់សេវាធនាគារចល័តដើម្បីរីករាយជាមួយប្រតិបត្តិការដ៏សម្បូរបែប។ ករណីមានចម្ងល់សូមទំនាក់ទំនងមកសេវាបម្រើអតិថិជន 070 200 002 ឬ 1800 200 888 ដើម្បីជំនួយបន្ថែម។";
        public static final String MSG_SYSTEM_BUSY = "ប្រព័ន្ធកំពុងមានបញ្ហាបណ្ដោះអាសន្ន។ សូមទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ "
                        + SUPPORT_PHONE_PRIMARY + " ឬ " + SUPPORT_PHONE_SECONDARY + " ដើម្បីទទួលបានជំនួយបន្ថែម។";
        public static final String MSG_CONNECTION_TIMEOUT = "ការតភ្ជាប់មានភាពយឺតយ៉ាវ។ សូមព្យាយាមម្តងទៀត។";

        // ==================================================================================
        // 6. AML MESSAGES
        // Used in: OpenAccountServiceImpl
        // ==================================================================================
        public static final String AML_NEED_REVIEW_MSG = "ការស្នើសុំបរាជ័យ\n" +
                        "ការស្នើសុំរបស់អ្នក (AML High Risk) ត្រូវការត្រួតពិនិត្យ។ ធនាគារនឹងឆ្លើយតបបន្ទាប់ពីត្រួតពិនិត្យរួចរាល់ ឬទំនាក់ទំនងមកសេវាបម្រើអតិថិជន 070 200 002 ឬ 1800 200 888 ដើម្បីជំនួយបន្ថែម។";
        public static final String AML_REJECTED_MSG = "ការត្រួតពិនិត្យ (AML High Risk) ត្រូវបានបដិសេធសម្រាប់អត្តសញ្ញាណប័ណ្ណលេខ %s។ គណនីមិនអាចបង្កើតបានទេ។ "
                        + SUPPORT_CONTACT;
        public static final String AML_UNKNOWN_MSG = "ស្ថានភាព (AML High Risk) មិនស្គាល់សម្រាប់អត្តសញ្ញាណប័ណ្ណលេខ %s។ សូមព្យាយាមម្តងទៀតនៅពេលក្រោយ ឬទំនាក់ទំនងសេវាបម្រើអតិថិជនតាមរយៈលេខ "
                        + SUPPORT_PHONE_PRIMARY + " ឬ " + SUPPORT_PHONE_SECONDARY + "។";

        // ==================================================================================
        // 7. OPEN ACCOUNT WORKFLOW STEPS
        // Used in: OpenAccountServiceImpl for tracking progress
        // ==================================================================================
        public static final String TEST_CONNECTION = "TEST_CONNECTION";
        public static final String PROCESS_AML = "PROCESS_AML";
        public static final String GET_CUSTOMER_INFO = "GET_CUSTOMER_INFO";
        public static final String VALIDATE_EXISTING_ACCOUNT = "VALIDATE_EXISTING_ACCOUNT";
        public static final String CREATE_CUSTOMER = "CREATE_CUSTOMER";
        public static final String CREATE_KHR_ACCOUNT = "CREATE_KHR_ACCOUNT";
        public static final String CREATE_USD_ACCOUNT = "CREATE_USD_ACCOUNT";
        public static final String VALIDATE_ACCOUNT_CREATION = "VALIDATE_ACCOUNT_CREATION";
        public static final String ACTIVATE_MOBILE_BANKING = "ACTIVATE_MOBILE_BANKING";
        public static final String SAVE_CUSTOMER_IMAGES = "SAVE_CUSTOMER_IMAGES";

        // ==================================================================================
        // 8. FIELD NAME TRANSLATIONS
        // Used in: CamdxErrorCheckServiceImpl
        // ==================================================================================
        public static final String FIELD_KH_LASTNAME_EN = "នាមត្រកូល (អង់គ្លេស)";
        public static final String FIELD_KH_FIRSTNAME_EN = "នាមខ្លួន (អង់គ្លេស)";
        public static final String FIELD_KH_DOB = "ថ្ងៃខែឆ្នាំកំណើត";
        public static final String FIELD_KH_GENDER = "ភេទ";

        public static final String FIELD_LAST_NAME_EN = "lastNameEn";
        public static final String FIELD_FIRST_NAME_EN = "firstNameEn";
        public static final String FIELD_DOB = "dob";
        public static final String FIELD_GENDER = "gender";

        public static final String FIELD_NONE = "None";
        public static final String BULLET_PREFIX = "- ";
        public static final String NEW_LINE = "\n";

        // T24 ERROR RESPONSE
        public static final String T24_ACCOUNT_ERROR = "SECURITY VIOLATION DURING SIGN ON PROCESS";

        private AppConstants() {

        }
}
