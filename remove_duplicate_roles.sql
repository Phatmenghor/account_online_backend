-- SQL Script to remove duplicate user-role assignments
-- Keeps only one instance of each unique (user_id, role_id) pair

-- Step 1: Check for duplicates before deletion
SELECT user_id, role_id, COUNT(*) as duplicate_count
FROM acc_online_user_roles
GROUP BY user_id, role_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Step 2: Delete duplicates, keeping only the first occurrence based on ctid
-- ctid is a system column that uniquely identifies each row
DELETE FROM acc_online_user_roles
WHERE ctid NOT IN (
    SELECT MIN(ctid)
    FROM acc_online_user_roles
    GROUP BY user_id, role_id
);

-- Step 3: Add a unique constraint to prevent future duplicates
ALTER TABLE acc_online_user_roles
ADD CONSTRAINT uk_user_role UNIQUE (user_id, role_id);

-- Step 4: Verify no duplicates remain
SELECT user_id, role_id, COUNT(*) as count
FROM acc_online_user_roles
GROUP BY user_id, role_id
HAVING COUNT(*) > 1;

-- This should return no rows if all duplicates were removed
