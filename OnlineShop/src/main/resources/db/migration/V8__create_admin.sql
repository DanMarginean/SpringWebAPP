INSERT INTO users (username, email, password)
VALUES ('admin', 'admin@example.com', '$2a$10$hashedPasswordHere');

INSERT INTO user_roles (user_id, role_id)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
       );
