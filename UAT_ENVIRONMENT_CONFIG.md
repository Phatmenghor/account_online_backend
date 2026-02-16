# UAT Environment Configuration

This document outlines the key configurations for the UAT environment based on `application.yaml`.

## Global Settings
- **Server Port**: `9393`
- **Context Path**: `/`
- **Timezone**: `Asia/Phnom_Penh` (implied from scheduler settings)

## Database Configuration (PostgreSQL)
- **URL**: `jdbc:postgresql://192.168.103.106:5432/account_online`
- **Username**: `postgres`
- **Driver**: `org.postgresql.Driver`
- **Connection Pool**: HikariCP (Max 20, Min 5)

## External Integrations

### Oracle Database (Data Warehouse / Staging)
- **URL**: `jdbc:oracle:thin:@//192.168.127.88:1521/stg`
- **Username**: `stg`
- **Pool**: Oracle-DWH-Pool / Oracle-STG-Pool

### T24 Core Banking
- **URL**: `http://192.168.127.31:7003`
- **Username**: `L.HOURNG`

### CAMDX (National ID Verification)
- **URL**: `http://192.168.103.106:8099/api/v1/`
- **Token Header**: `X-CPB-CamDx-Auth`
- **AML Check URL**: `http://192.168.103.106:9095/api/aml-check`

### Mobile Banking (MB)
- **OTP URL**: `http://192.168.103.11:15004/CPBBank/services/CPBMobile?wsdl`
- **Register Customer URL**: `http://192.168.103.13:8090/API/RegisterCustomerMB/`

### AML Dashboard
- **URL**: `http://192.168.103.106:8282/aml-management?pageNo=1`

### Telegram Bot
- **Bot Token**: `8227113200:AAGq1I7Hs0CjkSW99M9rgrIdlHpgM8r6NMY`
- **Chat IDs**:
    - UAT ACL / Monitor: `-4886328332`

## Application Features

### Simulator Configuration
Controls the simulation of various system behaviors for testing.
```yaml
simulator:
  report-logs: false       # Enable "Every Minute" report generation
  aml: false               # Simulate AML High Risk alert
  camdx: false             # Simulate CamDX Validation Failure
  banking:
    camdx-error: false     # Simulate CAMDX Failure in BankingService
    internal-error: false  # Simulate T24 Connection Error in BankingService
```

### File Upload Paths
- **Base Directory**: `/app/customer-image`
- **NID**: `/nid`
- **Selfie**: `/selfie`

### Email Configuration
- **Host**: `10.18.1.204`
- **Port**: `25`
- **Username**: `ithelpdesk@cambodiapostbank.com.kh`
- **Auth**: Enabled (TLS Disabled)

### CPB Business Rules
- **Branch Code**: `KH0012011`
- **Product Code**: `SAVE.ACCT.ONLINE`
- **Currency**: `KHR` / `USD` (implied usage)

## Security (JWT)
- **Issuer**: `account-online-api`
- **Expiration**: `525600` minutes (1 year)
