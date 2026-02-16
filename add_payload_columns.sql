-- Add request_payload and response_payload columns to acc_online_audit table

ALTER TABLE acc_online_audit
ADD COLUMN request_payload TEXT,
ADD COLUMN response_payload TEXT;
