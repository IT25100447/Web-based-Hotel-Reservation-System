-- ==========================================================
-- 1. General Manager (manager@smartstay.com)
-- 2. Front Office Manager (frontoffice@smartstay.com)
-- 3. Reservations Officer (reservations@smartstay.com)
-- 4. Customer Relations Executive (relations@smartstay.com)
-- 5. IT Coordinator (itcoordinator@smartstay.com & it@smartstay.com)
-- 6. Guest Customer (guest@smartstay.com)
-- ==========================================================

USE SmartStayDB;
GO

-- 0. Ensure password column sizes support full length strings
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Staff') AND name = 'password' AND max_length < 200)
BEGIN
    ALTER TABLE Staff ALTER COLUMN password NVARCHAR(255) NOT NULL;
END
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Guest') AND name = 'password' AND max_length < 200)
BEGIN
    ALTER TABLE Guest ALTER COLUMN password NVARCHAR(255) NOT NULL;
END
GO

-- 1. Ensure Branches exist
IF NOT EXISTS (SELECT 1 FROM Branch)
BEGIN
    INSERT INTO Branch (name, address, contact_number) VALUES
    ('SmartStay Colombo City', '100 Galle Road, Colombo 03, Sri Lanka', '+94 11 234 5678'),
    ('SmartStay Kandy Hilltop', '45 Rajapihilla Mawatha, Kandy, Sri Lanka', '+94 81 223 3445'),
    ('SmartStay Galle Beach Resort', '12 Light House Street, Galle Fort, Sri Lanka', '+94 91 224 8899');
    PRINT 'Branches seeded.';
END
GO

DECLARE @BranchId INT;
SELECT TOP 1 @BranchId = branch_id FROM Branch ORDER BY branch_id ASC;

-- 2. Seed / Reset General Manager
IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'manager@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'Nuwan Jayasinghe (General Manager)', 'manager@smartstay.com', '+94 77 111 2233', 'Password123!', 'MANAGER', GETDATE(), 'ACTIVE', 'LEVEL_3', 'EXECUTIVE');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'MANAGER' WHERE email = 'manager@smartstay.com';
END

-- 3. Seed / Reset Front Office Manager
IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'frontoffice@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'Chathuri Perera (Front Office Manager)', 'frontoffice@smartstay.com', '+94 77 333 4455', 'Password123!', 'FRONT_OFFICE', GETDATE(), 'ACTIVE', 'LEVEL_2', 'OPERATIONAL');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'FRONT_OFFICE' WHERE email = 'frontoffice@smartstay.com';
END

-- 4. Seed / Reset Reservations Officer
IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'reservations@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'Dilshan Fernando (Reservations Officer)', 'reservations@smartstay.com', '+94 77 444 5566', 'Password123!', 'RESERVATIONS', GETDATE(), 'ACTIVE', 'LEVEL_1', 'STANDARD');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'RESERVATIONS' WHERE email = 'reservations@smartstay.com';
END

-- 5. Seed / Reset Customer Relations Executive (Guest Relations)
IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'relations@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'Ishara Senanayake (Customer Relations)', 'relations@smartstay.com', '+94 77 555 6677', 'Password123!', 'GUEST_RELATIONS', GETDATE(), 'ACTIVE', 'LEVEL_2', 'OPERATIONAL');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'GUEST_RELATIONS' WHERE email = 'relations@smartstay.com';
END

-- 6. Seed / Reset IT Coordinator Accounts
IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'it@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'Kasun Wijeratne (IT Coordinator)', 'it@smartstay.com', '+94 77 222 3344', 'Password123!', 'IT', GETDATE(), 'ACTIVE', 'ADMIN', 'SYSTEM');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'IT' WHERE email = 'it@smartstay.com';
END

IF NOT EXISTS (SELECT 1 FROM Staff WHERE email = 'itcoordinator@smartstay.com')
BEGIN
    INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, access_level, approval_level)
    VALUES (@BranchId, NULL, N'IT Coordinator Admin', 'itcoordinator@smartstay.com', '+94 77 000 1122', 'Password123!', 'IT', GETDATE(), 'ACTIVE', 'ADMIN', 'SYSTEM');
END
ELSE
BEGIN
    UPDATE Staff SET password = 'Password123!', status = 'ACTIVE', role = 'IT' WHERE email = 'itcoordinator@smartstay.com';
END

-- 7. Seed / Reset Guest Account
IF NOT EXISTS (SELECT 1 FROM Guest WHERE email = 'guest@smartstay.com')
BEGIN
    INSERT INTO Guest (name, email, phone, address, password)
    VALUES (N'Kamal Perera (Guest)', 'guest@smartstay.com', '+94 71 999 8888', N'No 25 Temple Road, Nugegoda', 'Password123!');
END
ELSE
BEGIN
    UPDATE Guest SET password = 'Password123!' WHERE email = 'guest@smartstay.com';
END
GO

PRINT 'All 6 SLIIT Managing & Customer Accounts seeded successfully!';

-- Verification Query
SELECT 'STAFF' AS account_type, staff_id AS id, name, email, role, status FROM Staff
UNION ALL
SELECT 'GUEST' AS account_type, guest_id AS id, name, email, 'GUEST' AS role, 'ACTIVE' AS status FROM Guest;
GO
