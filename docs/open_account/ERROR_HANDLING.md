# Error Handling and Recovery

The `OpenAccountService` uses a centralized error-handling strategy to ensure data integrity and clear diagnostic feedback.

## 1. Transactional Integrity
The entire `openAccount` method is marked with `@Transactional`. 
- **Rollback**: If an exception occurs during critical steps (T24 CIF creation, Account creation, MB activation), the PostgreSQL database state is rolled back.
- **External State**: Note that T24 operations are external. If a CIF is created but the account fails, the CIF remains in T24. The system is designed to "Resume" by resolving the existing CIF in the next attempt.

## 2. Step-Based Error Tracking
We use a `currentStep` pointer to track progress.

| Step Pointer | Failure Implication | Recovery Action |
| :--- | :--- | :--- |
| `TEST_CONNECTION` | Infrastructure issue. | Retry after 5 minutes. |
| `GET_CUSTOMER_INFO` | Middleware or T24 timeout. | Check T24 connectivity. |
| `PROCESS_AML` | Regulatory block. | **Do NOT retry.** Check AML Dashboard. |
| `CREATE_CUSTOMER` | T24 duplicate or data error. | Verify NID/Name formatting. |
| `CREATE_KHR_ACCOUNT` | Product availability issue. | Check T24 Category codes. |

## 3. Failure Remarks
On every failure, the `ReportingFacade` builds a `failureRemark`.
- **Format**: `[STEP_NAME] Error: {Original Message} | CIF: {CIF if any} | Accounts: {Accounts if any}`
- **Purpose**: These remarks are saved to `success_log` (with fail status) to help IT support identify the root cause instantly.

## 4. Telegram Alerts
Failures trigger an automated Telegram alert to the `CPB-Monitor` group, unless the error is a standard "High Risk AML" block, which is considered a business outcome rather than a system failure.
