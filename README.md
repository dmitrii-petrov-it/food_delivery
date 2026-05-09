# Food Delivery CRUD

Minimal CRUD project for clients, couriers, and orders using PostgreSQL and JDBC.

## Database

Run the schema in your PostgreSQL database (database name: `food_delivery`).

```
DROP TABLE IF EXISTS orders, couriers, clients CASCADE;

CREATE TABLE clients (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255) NOT NULL
);

CREATE TABLE couriers (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'available'
        CHECK (status IN ('available', 'busy', 'inactive'))
);

CREATE TABLE orders (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    courier_id INT REFERENCES couriers(id) ON DELETE SET NULL,

    order_number VARCHAR(50) NOT NULL UNIQUE,
    restaurant_name VARCHAR(150) NOT NULL,
    food_description TEXT NOT NULL,

    delivery_address VARCHAR(255) NOT NULL,
    order_price NUMERIC(10,2) NOT NULL CHECK (order_price >= 0),
    delivery_fee NUMERIC(10,2) NOT NULL CHECK (delivery_fee >= 0),

    status VARCHAR(30) NOT NULL DEFAULT 'created'
        CHECK (status IN ('created', 'accepted', 'preparing', 'in_delivery', 'delivered', 'cancelled')),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP
);
```

## Run (console CRUD sample)

The `AppRunner` class demonstrates a minimal CRUD flow.

```
# from C:\Java\food_delivery
mvn -q -DskipTests compile
mvn -q -Dexec.mainClass=org.example.food_delivery.AppRunner exec:java
```

If you want to run the JavaFX UI entry point:

```
mvn -q -DskipTests javafx:run
```

