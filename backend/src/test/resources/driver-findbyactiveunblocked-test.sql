INSERT INTO users
(id, email, password, first_name, last_name, address, phone_number, role, blocked, block_reason, activated, created_at, updated_at)
VALUES
    (1, 'blocked@example.com', 'hashed_password', 'Blocked', 'Driver', 'Street', '1111111111', 'DRIVER', TRUE, 'Blocked', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'inactive@example.com', 'hashed_password', 'Inactive', 'Driver', 'Street', '2222222222', 'DRIVER', FALSE, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'active@example.com', 'hashed_password', 'Active', 'Driver', 'Street', '3333333333', 'DRIVER', FALSE, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO drivers
(id, available, active, worked_minutes_last24h)
VALUES
    (1, FALSE, TRUE, 120.0),   -- blocked driver
    (2, FALSE, FALSE, 60.0),   -- inactive driver
    (3, TRUE, TRUE, 180.0);    -- active & unblocked driver
