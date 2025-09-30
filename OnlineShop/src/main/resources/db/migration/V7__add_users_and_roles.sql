-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       role_id BIGINT NOT NULL REFERENCES roles(id)
);

-- Seed initial roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER');

