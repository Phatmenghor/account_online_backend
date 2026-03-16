# 📊 Comprehensive Monitoring System Guide

**For:** CPBank Account Opening Online System
**Main Dev Team Channel:** `-1002740302492`
**Status:** ✅ ACTIVE

---

## 🎯 What Gets Monitored

### 1. Account Opening Flow (Most Important)
Every step from customer submission to account activation is monitored:

```
🟢 ACCOUNT OPENING STARTED
├─ Legal ID: 171031245
├─ NID Image: document uploaded
└─ Time: 10:13:18.284

✅ ACCOUNT OPENING STEP: TEST_CONNECTION
✅ ACCOUNT OPENING STEP: GET_CUSTOMER_INFO
✅ ACCOUNT OPENING STEP: VALIDATE_EXISTING_ACCOUNT
✅ ACCOUNT OPENING STEP: PROCESS_AML
✅ ACCOUNT OPENING STEP: CREATE_CUSTOMER
✅ ACCOUNT OPENING STEP: CREATE_KHR_ACCOUNT
✅ ACCOUNT OPENING STEP: CREATE_USD_ACCOUNT
✅ ACCOUNT OPENING STEP: VALIDATE_ACCOUNT_CREATION
✅ ACCOUNT OPENING STEP: ACTIVATE_MOBILE_BANKING

🎉 ACCOUNT OPENING COMPLETED SUCCESSFULLY
├─ CIF: 1000154545
├─ KHR Account: 001234567890
├─ USD Account: 001234567891
├─ Duration: 28.45 seconds
└─ Status: ✅ COMPLETED
```

### 2. CAMDX NID Validation
- **Middleware Failures** → Error code, connection issues (SENT TO DEV TEAM)
- **Validation Failures** → Score mismatch, field mismatches (SENT TO DEV TEAM)
- **Success Cases** → Only logged, no alert

### 3. AML Compliance Check
- **High Risk Detection** → Detailed alert with rules triggered (SENT TO DEV TEAM)
- **Low Risk Approval** → Only logged, no alert
- **Middleware Errors** → Connection/service issues (SENT TO DEV TEAM)

### 4. T24 Banking Service
- All customer creation attempts
- Account creation (KHR + USD)
- Activation requests
- Errors and timeouts

### 5. User Access & Authentication
- Login attempts (success/failure)
- Failed authentication attempts
- Suspicious access patterns
- IP tracking

### 6. Database Operations
- Connection pool health
- Query failures
- Connection errors
- Timeout warnings

### 7. Performance Monitoring
- Requests taking > 5 seconds
- API call durations
- Component execution times
- Bottleneck detection

---

## 📱 Telegram Alert Channels

### **Dev Team Channel** (Main)
**Chat ID:** `-1002740302492`

**What gets sent:**
- 🟢 Account opening started/completed
- ✅ Step-by-step progress
- ❌ Step failures with root cause
- 🚨 CAMDX middleware/validation failures
- ⚠️ AML high-risk customers
- 🔴 T24 service errors
- ❌ Database connection issues
- ⚡ Slow requests (>5s)
- ❌ Failed authentication attempts

**Audience:** Engineering/DevOps team
**Action:** Investigate and resolve issues

---

### **Operations/Monitoring Channel** (Secondary)
**Chat ID:** `-1003174573947`

**What gets sent:**
- CAMDX summary failures (simple format)
- AML high-risk cases with photos
- Account opening summary (not detailed)

**Audience:** Operations team
**Action:** Contact customers, approve/reject applications

---

## 🔍 Reading the Alerts

### Alert Format Explained

```
🟢 ACCOUNT OPENING STARTED
═══════════════════════════════════════
├─ Legal ID: 171031245          ← Customer ID
├─ NID Image: nid_12345.jpg     ← Document name
├─ Selfie Image: selfie_1.jpg   ← Customer photo
└─ Time: 10:13:18.284           ← Asia/Phnom Penh time
═══════════════════════════════════════
```

**Emojis Mean:**
- 🟢 = Starting process
- ✅ = Step completed successfully
- ⚠️ = Warning/attention needed
- ❌ = Failed
- 🔴 = Critical error
- 🚨 = Emergency alert
- 🎉 = Success!
- ⚡ = Performance issue

---

## 📊 What Happens When...

### ✅ Account Opens Successfully
```
1. Green "STARTED" alert sent
2. Each step generates SUCCESS alert
3. Green "COMPLETED" alert with details
4. Customer gets account info
```

### ❌ Account Opening Fails
```
1. Each step sends progress alert
2. When failure occurs: RED ALERT with:
   - Which step failed
   - Error message
   - Root cause (if available)
   - Time
   - Action suggestions
3. Failure logged to database
4. Customer gets error message
```

### 🚨 Critical Infrastructure Issue
```
1. CAMDX Connection Error → Dev Team Alert
2. T24 Service Down → Dev Team Alert
3. Database Issue → Dev Team Alert
4. Slow Performance (>5s) → Dev Team Alert
5. Auth Failure → Dev Team Alert
```

---

## 💻 For Developers

### View Monitoring in Action

**Option 1: Monitor Telegram**
```
Open Chat: -1002740302492
Watch real-time alerts
```

**Option 2: Check Application Logs**
```bash
tail -f logs/account_online.log

# Look for:
# ========== ACCOUNT OPENING STARTED ==========
# ✅ ACCOUNT OPENING STEP
# ========== ACCOUNT OPENING COMPLETED ==========
# ========== ACCOUNT OPENING FAILED ==========
```

**Option 3: Query Database**
```sql
-- View all account opening attempts (last 24 hours)
SELECT legal_id, endpoint, method, status_code, duration_ms, created_at
FROM request_log
WHERE endpoint LIKE '%open-account%'
AND created_at > NOW() - INTERVAL '1 day'
ORDER BY created_at DESC;

-- View failed requests
SELECT endpoint, status_code, error_message, created_at
FROM request_log
WHERE is_success = false
ORDER BY created_at DESC LIMIT 20;

-- View AML high-risk customers
SELECT legal_id, risk_level, status, rules_triggered, created_at
FROM aml_status
WHERE status = 'PENDING'
ORDER BY created_at DESC;
```

---

## 🎯 Key Monitoring Points

| Component | What's Monitored | Alert Level |
|-----------|-------------------|------------|
| **Account Opening** | Every step | HIGH |
| **CAMDX NID Validation** | Success/Failure | HIGH |
| **AML Compliance** | Risk assessment | HIGH |
| **T24 Banking** | Service calls | HIGH |
| **Authentication** | Login attempts | MEDIUM |
| **Database** | Connections | MEDIUM |
| **Performance** | Slow requests | LOW |
| **User Access** | Suspicious activity | MEDIUM |

---

## 🛠️ Troubleshooting

### Issue: Not Getting Alerts?

**Check 1: Is chat ID configured?**
```yaml
# application-uat.yaml
telegram:
  bot:
    uat-dev-team-chat-id: "-1002740302492"  # Should be set
```

**Check 2: Are logs showing?**
```bash
grep "ACCOUNT OPENING" logs/account_online.log
```

**Check 3: Is Telegram bot token valid?**
```bash
# Check application logs for Telegram errors
grep "Telegram" logs/account_online.log | grep -i error
```

### Issue: Alerts Suddenly Stopped?

1. Check Telegram bot token (may have expired)
2. Check internet connectivity
3. Check if chat ID is still valid
4. Review application logs for errors
5. Restart the application

---

## 📈 Performance Baselines

**Expected Durations:**
- Account opening: 20-35 seconds
- CAMDX validation: 1-3 seconds
- AML check: 2-5 seconds
- T24 customer creation: 3-7 seconds
- T24 account creation: 2-5 seconds

**Alert Thresholds:**
- Performance alert: >5 seconds
- Connection timeout: >30 seconds
- Service unavailable: Immediate

---

## 🔐 Security Notes

**Monitored Events:**
- Authentication failures
- Suspicious IP addresses
- Repeated failed login attempts
- Unusual access patterns
- Data access violations

**What's NOT Sent to Telegram:**
- Customer PII (except for investigation alerts)
- Account numbers in main alerts
- Full request/response bodies
- Sensitive data

---

## 📋 Monitoring Checklist

Daily:
- [ ] Check for failed account openings
- [ ] Review AML high-risk alerts
- [ ] Check CAMDX validation failures
- [ ] Monitor T24 service health
- [ ] Check database connection pool

Weekly:
- [ ] Review performance trends
- [ ] Check for slow requests
- [ ] Review authentication patterns
- [ ] Check system uptime
- [ ] Review error logs

Monthly:
- [ ] Capacity planning review
- [ ] Performance optimization
- [ ] Security audit
- [ ] Alert threshold review

---

## 📞 Support

**For Monitoring Questions:**
- Check logs: `logs/account_online.log`
- Check database: `request_log` table
- Review Telegram alerts for details
- Contact DevOps team

**For Critical Issues:**
- Check Telegram immediately
- Review error message
- Check root cause if available
- Contact system administrator

---

**Last Updated:** 2026-03-16
**Monitoring Status:** ✅ ACTIVE & FULLY FUNCTIONAL

All alerts go to: `-1002740302492` Dev Team Channel
