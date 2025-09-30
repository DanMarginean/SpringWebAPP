ALTER TABLE customers
    ADD COLUMN user_id BIGINT;

DELETE FROM customers WHERE user_id IS NULL;

ALTER TABLE customers
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE customers
    ADD CONSTRAINT uq_customer_user UNIQUE (user_id);

ALTER TABLE customers
    ADD CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id)
            REFERENCES users(id);