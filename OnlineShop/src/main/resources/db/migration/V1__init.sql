CREATE table products (
    id SERIAL PRIMARY KEY ,
    name varchar(255) not null ,
    price numeric(10,2) not null ,
    description text,
    created_at timestamp default current_timestamp
);