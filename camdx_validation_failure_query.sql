-- =====================================================
-- CAMDX VALIDATION FAILURE QUERY
-- =====================================================
-- This query searches for the failed submission record
-- Based on Telegram Alert:
-- NID: 062229212
-- Name KH: Tho Rathana
-- Name EN: Vorn buntheoun 𝐇𝐮𝐬𝐛𝐮𝐧𝐝🥰
-- DOB: 2026-02-14
-- Gender: M
-- Time: 2026-02-14 17:00:06
-- =====================================================

-- Query 1: Search in acc_online_open_final by NID
SELECT 
    id,
    created_at,
    legal_id AS nid,
    legal_first_name_kh,
    legal_last_name_kh,
    legal_first_name_en,
    legal_last_name_en,
    legal_date_of_birth AS dob,
    legal_gender AS gender,
    legal_issued_date,
    legal_expired_date,
    cif,
    khr_account,
    usd_account,
    aml_status,
    phone_number
FROM acc_online_open_final
WHERE legal_id = '062229212'
ORDER BY created_at DESC;

-- =====================================================

-- Query 2: Search by NID with approximate time range (within 1 hour of failure)
SELECT 
    id,
    created_at,
    legal_id AS nid,
    legal_first_name_kh,
    legal_last_name_kh,
    legal_first_name_en,
    legal_last_name_en,
    legal_date_of_birth AS dob,
    legal_gender AS gender,
    legal_issued_date,
    legal_expired_date,
    cif,
    aml_status
FROM acc_online_open_final
WHERE legal_id = '062229212'
  AND created_at BETWEEN '2026-02-14 16:00:00' AND '2026-02-14 18:00:00'
ORDER BY created_at DESC;

-- =====================================================

-- Query 3: Search by multiple criteria (loose match for corrupted name)
SELECT 
    id,
    created_at,
    legal_id AS nid,
    legal_first_name_kh,
    legal_last_name_kh,
    legal_first_name_en,
    legal_last_name_en,
    legal_date_of_birth AS dob,
    legal_gender AS gender,
    cif,
    aml_status
FROM acc_online_open_final
WHERE (
    legal_id = '062229212'
    OR (legal_date_of_birth = '2026-02-14' AND legal_gender = 'M')
    OR (legal_first_name_kh LIKE '%Tho%' AND legal_last_name_kh LIKE '%Rathana%')
    OR legal_first_name_en LIKE '%Vorn%'
    OR legal_last_name_en LIKE '%buntheoun%'
)
AND created_at >= '2026-02-14 00:00:00'
ORDER BY created_at DESC
LIMIT 20;

-- =====================================================

-- Query 4: Search in audit logs (acc_online_audit) for API calls around that time
SELECT 
    id,
    created_at,
    username,
    endpoint,
    method,
    status_code,
    is_success,
    error_message,
    duration_ms
FROM acc_online_audit
WHERE created_at BETWEEN '2026-02-14 16:00:00' AND '2026-02-14 18:00:00'
  AND (
    endpoint LIKE '%camdx%' 
    OR endpoint LIKE '%nid%' 
    OR endpoint LIKE '%validate%'
  )
ORDER BY created_at DESC;

-- =====================================================

-- Query 5: Combined view - Join Final Account with Audit logs
SELECT 
    aof.id,
    aof.created_at AS submission_time,
    aof.legal_id AS nid,
    CONCAT(aof.legal_first_name_kh, ' ', aof.legal_last_name_kh) AS name_kh,
    CONCAT(aof.legal_first_name_en, ' ', aof.legal_last_name_en) AS name_en,
    aof.legal_date_of_birth AS dob,
    aof.legal_gender AS gender,
    aof.cif,
    aof.aml_status,
    aa.endpoint AS api_endpoint,
    aa.status_code,
    aa.error_message,
    aa.username AS submitted_by
FROM acc_online_open_final aof
LEFT JOIN acc_online_audit aa 
    ON DATE_TRUNC('minute', aof.created_at) = DATE_TRUNC('minute', aa.created_at)
WHERE aof.legal_id = '062229212'
ORDER BY aof.created_at DESC;

-- =====================================================

-- Query 6: Search all submissions on 2026-02-14 
-- (to identify if there are multiple attempts)
SELECT 
    id,
    created_at,
    legal_id AS nid,
    legal_first_name_kh,
    legal_last_name_kh,
    legal_first_name_en,
    legal_last_name_en,
    legal_date_of_birth AS dob,
    legal_gender AS gender,
    cif,
    aml_status,
    phone_number
FROM acc_online_open_final
WHERE DATE(created_at) = '2026-02-14'
  AND (
    legal_id = '062229212'
    OR legal_first_name_kh LIKE '%Tho%'
    OR legal_first_name_en LIKE '%Vorn%'
  )
ORDER BY created_at DESC;

-- =====================================================

-- Query 7: Full record details for NID 062229212
SELECT *
FROM acc_online_open_final
WHERE legal_id = '062229212'
ORDER BY created_at DESC
LIMIT 1;
