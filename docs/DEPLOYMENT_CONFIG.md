# Deployment and Testing Guide

This document contains critical infrastructure, deployment paths, and manual testing specifications for the Account Online Backend.

## 1. Server Infrastructure

| Environment | IP Address | Deployment Path | Storage (Customer Images) |
| :--- | :--- | :--- | :--- |
| **UAT** | `192.168.103.106` | `/DATA/deployments/backend/account_online_backend` | `/DATA/files/account_online/customer-image` |
| **PROD** | `192.168.101.5` | `/otp/open_account_online` | `/Oracle_BI/project/open_account_online` |

---

## 2. Integrated Services (UAT vs PROD)

The system connects to various core services. Use the correct IP based on the environment:

| Service | UAT URL/IP | Production URL/IP |
| :--- | :--- | :--- |
| **Mobile Banking (OTP)** | `192.168.103.11` | `10.18.1.47` |
| **Mobile Banking (Register)** | `192.168.103.13` | `10.18.1.48` |
| **CamDx API** | `192.168.103.106:8099` | `192.168.127.86:8099` |
| **AML API** | `192.168.103.106:9095` | `data-intuition.cambodiapostbank.com.kh` |
| **T24 Middleware** | `192.168.127.31:7003` | `192.168.101.21:7003` |

---

## 3. SoapUI Manual Testing

Use these details to verify the core services manually.

### A. Create Account (T24)
- **WSDL URL**: `http://192.168.127.31:7003/TWS.CPBOAO/T24WebServicesImplService?WSDL`
- **Operation**: `OAOSPECIMENCREATION`
- **Request Body (XML)**: `src/main/resources/xml/createCustomerAccount`

### B. Send OTP (Sms Gateway)
- **WSDL URL**: `http://192.168.103.11:15004/CPBBank/services/CPBMobile?wsdl`
- **Operation**: `sendSms`
- **Request Body (XML)**: `src/main/resources/xml/SendOtp`

---

## 4. Legacy System Reference (Heritage)

This project replaces the old reference system with the following components:

- **Legacy Backend**: `oao-api-v2`
    - Language: C# (.NET)
- **Legacy Frontend**: `account-online-v2`
    - Framework: Spring
- **Legacy Primary URL**: [https://acc.cambodiapostbank.com/OpenAcct/](https://acc.cambodiapostbank.com/OpenAcct/)

---

## 5. Databases & Data Sources

### A. PostgreSQL (Primary Application DB)
Stores application data, user sessions, audit logs, and AML statuses.
- **UAT**: `192.168.103.106:5432` (Database: `account_online`)
- **PROD**: Managed on PROD server `192.168.101.5`

### B. Oracle Database (Staging/Master Data)
Used for location lookups (Provinces/Districts) and core staging data.
- **UAT**: `192.168.127.88:1521` (Service: `stg`)
- **PROD**: `192.168.102.5:1521` (Service: `stg`)
- **Schema**: `DWH`

### C. T24 (Core Banking)
The ultimate source of truth for CIFs and accounts.
- **UAT Middleware**: `192.168.127.31:7003`
- **PROD Middleware**: `192.168.101.21:7003`

---

## 6. Application Configuration Summary

### Server
- **Port**: `9000`
- **Context Path**: `/`

### Image Sub-folders
- **NID**: `/nid`
- **Selfie**: `/selfie`

---

## 6. Communication & Alerts
- **Telegram Bot Token**: `8227113200:AAGq1I7Hs0CjkSW99M9rgrIdlHpgM8r6NMY`
- **UAT Monitor Chat ID**: `-4886328332`
- **Email Server**: `10.18.1.204:25`

---

## 7. Key Security Settings
- **JWT Expiration**: 525,600 minutes (1 year)
- **Log Retention**: 30 days
- **OTP Cooldown**: 60 seconds
