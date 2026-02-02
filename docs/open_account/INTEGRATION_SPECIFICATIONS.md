# Integration Specifications

Detailed technical specifications for external system interactions within the Open Account module.

## 1. T24 (Core Banking)
- **Protocol**: SOAP/XML via T24 Web Services (TWS).
- **Primary Operation**: `OAOSPECIMENCREATION`.
- **Logic**:
    - **Customer Matching**: Matches by `LEGAL.ID` and `LEGAL.DOC.NAME`.
    - **Account Mapping**: Uses Category `1001` for Savings KHR and `1002` for Savings USD.

## 2. AML Screening (Middleware)
- **Enpoint**: `POST /api/aml-check`
- **Security**: Bearer Token (JWT).
- **Trigger**: Called directly before CIF creation.
- **Decision Logic**:
    - If risk score > threshold: Status is `PENDING` (needs manual review).
    - If risk score < threshold: Status is `APPROVE` (auto-proceeds).

## 3. Mobile Banking Gateway
- **WSDL Operation**: `sendSms`.
- **Registration**: `POST /API/RegisterCustomerMB/`.
- **Dependency**: Requires a valid CIF and at least one active account number.
- **Auth**: Secured via `secretKey` signature in the header.

## 4. CamDx (National Identity Service)
- **Usage**: Used to verify the authenticity of the NID image provided by the user.
- **Route**: `validate-nid-face` (Face matching) and `ocr-idcard` (Data extraction).
- **Integration**: Called early in the flow to populate the `OpenAccountContext` with verified identity data.
