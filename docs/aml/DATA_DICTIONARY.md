# AML Data Dictionary

This document defines the data structures and fields used within the AML (Anti-Money Laundering) module.

## 1. Input Data (`CreateAmlRequestDto`)
This data is typically provided when a new AML screening is initiated.

| Field | Type | Description |
| :--- | :--- | :--- |
| `legalId` | String | Customer's Identification Number. |
| `familyName` | String | Customer's family name. |
| `givenName` | String | Customer's given name. |
| `status` | Enum | Initial status (e.g., `PENDING`). |
| `phoneNumber` | String | Contact number. |
| `occupationCode` | String | Standardized code for customer occupation. |
| `riskLevel` | String | Preliminary risk level from automated screening. |
| `serviceName` | String | The name of the external AML engine used. |
| `trxnID` | String | External transaction ID for the screening request. |

## 2. Address & Location Data
The system resolves raw codes into these fields before saving.

| Field | Description |
| :--- | :--- |
| `currentAddressName` | Full human-readable current address. |
| `currentAddressCode` | Concatenated location codes (Prov-Dist-Comm-Vill). |
| `placeOfBirthName` | Full human-readable place of birth address. |
| `placeOfBirthCode` | Concatenated POB location codes. |

## 3. Results & Monitoring (`AmlStatusDto`)
Data returned when querying the current status or history.

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | Long | Internal database primary key. |
| `status` | Enum | Current status: `PENDING`, `APPROVE`, `REJECT`. |
| `screeningResult` | String | Detailed text response from external screening. |
| `totalRulesScore` | Integer | Total numeric score calculated by AML rules. |
| `approvedBy` | User | Details of the Compliance Officer who approved. |
| `rejectedBy` | User | Details of the Compliance Officer who rejected. |
| `remarks` | String | Internal notes/comments describing the decision. |

## 4. Derived & Event Data
| Field | Description |
| :--- | :--- |
| `AmlStatusChangedEvent` | Signal sent to triggers side-effects (Telegram, Logs). |
| `amlDto` | The payload of the status change event. |
