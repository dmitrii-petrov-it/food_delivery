CREATE TABLE IF NOT EXISTS clients (
    id         SERIAL PRIMARY KEY,
    full_name  VARCHAR(255) NOT NULL,
    phone      VARCHAR(64)  NOT NULL,
    email      VARCHAR(255),
    address    VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS couriers (
    id           SERIAL PRIMARY KEY,
    full_name    VARCHAR(255) NOT NULL,
    phone        VARCHAR(64)  NOT NULL,
    vehicle_type VARCHAR(32)  NOT NULL CHECK (vehicle_type IN ('bike', 'car', 'scooter')),
    status       VARCHAR(32)  NOT NULL CHECK (status IN ('available', 'busy', 'inactive')),
    photo        BYTEA
);

ALTER TABLE couriers ADD COLUMN IF NOT EXISTS photo BYTEA;

CREATE TABLE IF NOT EXISTS orders (
    id                SERIAL PRIMARY KEY,
    client_id         INTEGER       NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    courier_id        INTEGER       REFERENCES couriers (id) ON DELETE SET NULL,
    order_number      VARCHAR(64)   NOT NULL UNIQUE,
    restaurant_name   VARCHAR(255)  NOT NULL,
    food_description  TEXT,
    delivery_address  VARCHAR(512)  NOT NULL,
    order_price       NUMERIC(12, 2) NOT NULL,
    delivery_fee      NUMERIC(12, 2) NOT NULL,
    status            VARCHAR(32)   NOT NULL CHECK (status IN ('created', 'accepted', 'preparing', 'in_delivery', 'delivered', 'cancelled')),
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at      TIMESTAMP
);
