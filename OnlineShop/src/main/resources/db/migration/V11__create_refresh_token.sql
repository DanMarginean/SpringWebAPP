CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                expiry_date TIMESTAMP NOT NULL,
                                CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
