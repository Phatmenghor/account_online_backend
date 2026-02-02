# Open Account Data Dictionary (Technical Specification)

This document provides a detailed mapping of all data fields used in the `OpenAccountService`.

## 1. Input Data (`CustomerRequest`)

These fields are received via the frontend API.

| Field | Type | Required | Format / Business Rule |
| :--- | :--- | :---: | :--- |
| **Personal Info** | | | |
| `familyName` | String | Yes | Latin characters. Used for T24 legal name. |
| `givenName` | String | Yes | Latin characters. Used for T24 legal name. |
| `firstNameKh` | String | Yes | Khmer Unicode characters. |
| `lastNameKh` | String | Yes | Khmer Unicode characters. |
| `dateOfBirth` | String | Yes | `YYYY-MM-DD`. Must match NID data. |
| `gender` | String | Yes | `Male`, `Female`, or `Other`. |
| `maritalStatus` | String | No | Single, Married, etc. |
| `occupation` | String | No | Occupation code from master data. |
| **Identity / Document** | | | |
| `legalId` | String | Yes | National ID or Passport Number. **Primary Key.** |
| `legalDocType` | String | No | e.g., `NID`, `PSS`. |
| `legalIssueDate` | String | No | `YYYY-MM-DD`. |
| `legalExpireDate` | String | No | `YYYY-MM-DD`. |
| `legalAddress` | String | No | Full address string from ID card. |
| `legalMrz1`/`2`/`3` | String | No | Machine Readable Zone data for passports. |
| **Address (Current & POB)** | | | |
| `customerCurrentProvince`| String | No | Location code (e.g., `01`). |
| `customerCurrentDistrict`| String | No | Location code (e.g., `0101`). |
| `customerCurrentCommune` | String | No | Location code (e.g., `010101`). |
| `customerCurrentVillage` | String | No | Location code (e.g., `010101001`). |
| **Images (Base64)** | | | |
| `nidImage` | String | Yes | Base64 string of the ID card (front). |
| `selfieImage` | String | Yes | Base64 string of the live selfie. |
| **Meta / Referral** | | | |
| `phoneNumber` | String | Yes | Customer's primary mobile number. |
| `branchCode` | String | No | 4-digit CPB branch code. |
| `referralId` | String | No | Employee staff code for referral tracking. |

---

## 2. Internal State (`OpenAccountContext`)

Used for orchestrating data between service steps.

| Field | Description | Mapping / Logic |
| :--- | :--- | :--- |
| `customerInfo` | T24 Customer Record | Map of existing data found during the matching phase. |
| `amlResult` | Screening Result | Full JSON object from the AML screening engine. |
| `cif` | T24 Customer ID | Generated or resolved CIF number. |
| `mnemonic` | Customer Mnemonic | Unique human-readable ID in T24 (e.g., `JDOE12345`). |
| `khrAccount` | KHR Account Number | Format: `00x-xxxxxxx-x-x`. Category: `1001`. |
| `usdAccount` | USD Account Number | Format: `00x-xxxxxxx-x-x`. Category: `1002`. |

---

## 3. API Response (`CustomerResponse`)

The data returned to the caller upon success.

| Field | Description | Example |
| :--- | :--- | :--- |
| `cif` | The unique Customer ID. | `1234567` |
| `khrAccount` | The Khmer Riel Savings Account. | `001-002345-0-1` |
| `usdAccount` | The US Dollar Savings Account. | `001-002345-0-2` |
| `mnemonic` | Searchable Customer Name in T24. | `SMITH.JOHN.99` |

---

## 4. Internal Diagnostics

| Field | Purpose | Usage |
| :--- | :--- | :--- |
| `failureRemark` | Error Detail | Captured in `success_log` if the process fails. |
| `currentStep` | Pipeline Step | Tracks exactly where the code failed (e.g., `CREATE_CIF`). |
| `imageUrls` | Storage Links | Final file system paths saved to the database. |
