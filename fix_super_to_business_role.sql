-- SQL Script to update SUPER role to BUSINESS role
-- Run this script on your database to fix the enum mismatch issue

-- Update the roles table (acc_online_roles)
UPDATE acc_online_roles SET name = 'BUSINESS' WHERE name = 'SUPER';

-- Update menu_roles junction table (acc_online_menu_roles)
-- This table links menus to roles
UPDATE acc_online_menu_roles SET role = 'BUSINESS' WHERE role = 'SUPER';

-- Verify the changes
SELECT * FROM acc_online_roles WHERE name = 'BUSINESS';
SELECT * FROM acc_online_menu_roles WHERE role = 'BUSINESS';
