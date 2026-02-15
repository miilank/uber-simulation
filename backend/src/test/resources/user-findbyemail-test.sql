INSERT INTO users
(id, email, password, first_name, last_name, address, phone_number, role, blocked, block_reason, activated, created_at, updated_at)
VALUES
    (1, 'correct@example.com', 'hashed_password', 'Correct', 'User', 'Street', '1111111111', 'PASSENGER', FALSE, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'wrong@example.com', 'hashed_password', 'Wrong', 'User', 'Street', '2222222222', 'PASSENGER', FALSE, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO passengers
(id)
VALUES (1),
       (2);