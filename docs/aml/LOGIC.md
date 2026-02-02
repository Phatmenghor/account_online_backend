# AML Business Logic and Statuses

This document details the logic flow and status definitions for the AML module.

## 1. AML Status Lifecycle

The system tracks the progress of a customer's AML screening using the following statuses:

| Status | Description | User Action Required? |
| :--- | :--- | :--- |
| **PENDING** | Initial state when a request is first created. Waiting for review. | Yes (Compliance Team) |
| **APPROVE** | The customer has passed the screening. They can proceed to open accounts. | No |
| **REJECT** | The customer is high-risk and denied. Account opening is blocked. | No |

---

## 2. The Logic Flow

### A. Creation Phase
When `createAmlStatus` is called:
1.  **Address Normalization**: The service uses `MasterDataServiceHelper` to convert raw province/district codes into full human-readable names.
2.  **Audit Trail**: An initial record is created in `aml_history` with the `PENDING` status.

### B. Decision Phase (Manual Update)
When a Compliance Officer updates the status:
1.  **Security Check**: The `securityUtils` identifies the current logged-in user.
2.  **Status Branching**: 
    - If **APPROVE**: `approvedBy` is set to the current user, and `rejectedBy` is cleared.
    - If **REJECT**: `rejectedBy` is set to the current user, and `approvedBy` is cleared.
3.  **History Recording**: A new entry is added to `aml_history` to track who made the change and why (remarks).

---

## 3. Asynchronous Side Effects
Once the status is saved, the system triggers the following background tasks:

1.  **Final Log Synchronization**: Updates the `AccountOnlineOpenFinalService` so the centralized reporting table reflects the latest AML decision.
2.  **Telegram Notification**: Sends an instant alert to the Compliance group with the decision, legal ID, and officer name.

---

## 4. External Risk Assessment
The module also supports external updates. If an external screening service (e.g., middleware) returns a "Low Risk" result, the status can be automatically updated to **APPROVE** via the `updateExternalAmlStatus` endpoint.
