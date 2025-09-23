CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       customer_id BIGINT NOT NULL UNIQUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
                            CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);
