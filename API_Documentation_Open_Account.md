# API Documentation: Open Account API

## Introduction

**Purpose:** This document provides a comprehensive overview of the Open Account API, including available endpoints, request/response formats, authentication requirements, and other technical specifications intended to assist developers and system integrators in utilizing the API effectively.

## General Information

- **API Base URL:** `{YOUR_BASE_URL}/api/v1/public`
- **API Version:** v1
- **Contact Email:** {YOUR_CONTACT_EMAIL}
- **Authentication Method:** None (Public Endpoint)

## Authentication

This is a **public endpoint** and does not require authentication headers. The endpoint is accessible without API keys or bearer tokens.

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Validation Error | One or more required fields are missing or invalid |
| 500 | Internal Server Error | An unexpected error occurred during account creation |

## Resources / Endpoints

### Open Account

**Method:** `POST`

**URL:** `/api/v1/public/open-account`

**Description:** Creates a new customer account with KHR and USD accounts in the T24 banking system. This endpoint performs comprehensive validation including NID verification, AML screening, customer creation, account creation, and mobile banking activation.

#### Request Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `familyName` | String | Yes | Customer's family name (last name) in English |
| `givenName` | String | Yes | Customer's given name (first name) in English |
| `firstNameKh` | String | Yes | Customer's first name in Khmer |
| `lastNameKh` | String | Yes | Customer's last name in Khmer |
| `dateOfBirth` | String | Yes | Date of birth in format YYYY-MM-DD (e.g., "2005-01-09") |
| `gender` | String | Yes | Gender (e.g., "M" for Male, "F" for Female) |
| `placeOfBirth` | String | No | Place of birth |
| `companyName` | String | No | Company name (if employed) |
| `referralId` | String | No | Staff code of referral person |
| `branchCode` | String | No | Branch code |
| `occupation` | String | No | Occupation code |
| `maritalStatus` | String | No | Marital status |
| `customerCurrentProvince` | String | No | Current address - Province code |
| `customerCurrentDistrict` | String | No | Current address - District code |
| `customerCurrentCommune` | String | No | Current address - Commune code |
| `customerCurrentVillage` | String | No | Current address - Village code |
| `customerPobProvince` | String | No | Place of birth - Province code |
| `customerPobDistrict` | String | No | Place of birth - District code |
| `customerPobCommune` | String | No | Place of birth - Commune code |
| `customerPobVillage` | String | No | Place of birth - Village code |
| `legalId` | String | Yes | National ID number |
| `legalIssueDate` | String | No | ID issue date in format YYYY-MM-DD |
| `legalExpireDate` | String | No | ID expiry date in format YYYY-MM-DD |
| `legalAddress` | String | No | Legal address as per ID |
| `legalDocType` | String | No | Legal document type |
| `legalMrz1` | String | No | MRZ line 1 from ID |
| `legalMrz2` | String | No | MRZ line 2 from ID |
| `legalMrz3` | String | No | MRZ line 3 from ID |
| `phoneNumber` | String | Yes | Customer's phone number |
| `nidImage` | String | Yes | Base64 encoded National ID image |
| `selfieImage` | String | Yes | Base64 encoded selfie image |

#### Example Request:

```json
{
  "familyName": "Doe",
  "givenName": "John",
  "firstNameKh": "ជន",
  "lastNameKh": "ដូ",
  "dateOfBirth": "1990-05-15",
  "gender": "M",
  "placeOfBirth": "Phnom Penh",
  "occupation": "001",
  "maritalStatus": "Single",
  "customerCurrentProvince": "01",
  "customerCurrentDistrict": "0101",
  "customerCurrentCommune": "010101",
  "customerCurrentVillage": "01010101",
  "customerPobProvince": "01",
  "customerPobDistrict": "0101",
  "customerPobCommune": "010101",
  "customerPobVillage": "01010101",
  "legalId": "123456789012",
  "legalIssueDate": "2020-01-01",
  "legalExpireDate": "2030-01-01",
  "legalAddress": "Street 123, Phnom Penh",
  "phoneNumber": "012345678",
  "nidImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "selfieImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

#### Example Response:

**Success Response (200 OK):**

```json
{
  "status": "success",
  "message": "Account opened successfully",
  "data": {
    "cif": "100012345",
    "khrAccount": "0011234567890",
    "usdAccount": "0021234567890",
    "mnemonic": "JOHNDOE001"
  }
}
```

**Error Response (400 Bad Request):**

```json
{
  "status": "error",
  "message": "Family name is required",
  "data": null
}
```

**Error Response (500 Internal Server Error):**

```json
{
  "status": "error",
  "message": "Account opening failed at: CREATE_CUSTOMER | Error: Customer creation failed",
  "data": null
}
```

## Account Opening Process Flow

The API performs the following steps sequentially:

1. **Test Connection** - Validates database connectivity
2. **Get Customer Info** - Validates NID through CAMDX service
3. **Process AML** - Performs AML screening via external middleware
4. **Create Customer** - Creates customer record in T24
5. **Create KHR Account** - Creates KHR currency account
6. **Create USD Account** - Creates USD currency account
7. **Validate Accounts** - Ensures at least one account was created
8. **Activate Mobile Banking** - Activates mobile banking service
9. **Save Customer Images** - Stores NID and selfie images
10. **Save Final Log** - Logs successful account creation

If any step fails, the process is aborted and an error response is returned.

## Change Log

- **2026-01-05**: Added DOB parsing for AML middleware (dobYear, dobMonth, dobDay)
- **2025-12-26**: Added external AML status update API integration
- **Initial Release**: Base account opening functionality

## Glossary / Definitions

- **CIF**: Customer Information File - Unique customer identifier in T24
- **Mnemonic**: Short alphanumeric code representing the customer
- **AML**: Anti-Money Laundering screening process
- **NID**: National ID - Cambodia National Identity Document
- **T24**: Core banking system
- **CAMDX**: Cambodia Data Exchange - National ID verification service
- **MRZ**: Machine Readable Zone - Data encoded in ID documents
- **KHR**: Cambodian Riel currency code
- **USD**: United States Dollar currency code
