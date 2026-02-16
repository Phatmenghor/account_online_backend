-- Query to find the specific CamDX validation failure based on the provided logs
-- Timestamp: 2026-02-16 14:38:11
-- ID: 010124374

SELECT 
    id,
    created_at,
    username,
    ip_address,
    method,
    endpoint,
    status_code,
    duration_ms,
    is_success,
    error_message,
    request_payload,
    response_payload
FROM public.acc_online_audit
WHERE created_at BETWEEN '2026-02-16 14:38:00' AND '2026-02-16 14:39:00'
  AND (
      endpoint LIKE '%validate%' 
      OR request_payload LIKE '%010124374%'
  )
ORDER BY created_at DESC;
