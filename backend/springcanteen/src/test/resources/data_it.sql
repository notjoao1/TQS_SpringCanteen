/* CREATE TYPE Employee_role AS ENUM('COOK', 'DESK_PAYMENTS', 'DESK_ORDERS');
CREATE TYPE Order_status AS ENUM('IDLE', 'PREPARING', 'READY', 'PICKED_UP');
CREATE CAST (CHARACTER VARYING as Employee_role) WITH INOUT AS IMPLICIT;
CREATE CAST (CHARACTER VARYING as Order_status) WITH INOUT AS IMPLICIT;


CREATE TABLE employees (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(255) not null,
    email varchar(255) not null,
    password varchar(63) not null,
    role Employee_role not null
);

CREATE TABLE kiosk_terminals (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

CREATE TABLE ingredients (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(31) not null,
    price numeric(5, 2) not null,
    calories integer not null
);

CREATE TABLE items (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(31) not null,
    price numeric(5, 2) not null
);

CREATE TABLE drinks (
    id integer references items(id),
    PRIMARY KEY (id)
);

CREATE TABLE main_dishes (
    id integer references items(id),
    PRIMARY KEY (id)
);

CREATE TABLE maindish_ingredients(
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id integer references main_dishes(id),
    ingredient_id integer references ingredients(id),
    quantity integer not null
);

CREATE TABLE menus (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(31) not null,
    image_link varchar(512) not null
);

CREATE TABLE menu_drinks(
    menu_id integer references menus(id),
    item_id integer references items(id),
    PRIMARY KEY (menu_id, item_id)
);

CREATE TABLE menu_main_dishes(
    menu_id integer references menus(id),
    item_id integer references items(id),
    PRIMARY KEY (menu_id, item_id)
);

CREATE TABLE orders (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_status Order_status,
    price numeric(5, 2) not null,
    is_paid boolean not null,
    is_priority boolean not null,
    nif varchar(9) not null,
    kiosk_id integer not null
);

CREATE TABLE order_menus(
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id integer references orders(id),
    menu_id integer references menus(id),
    customization pg_catalog.jsonb not null
);


INSERT INTO employees (name, email, password, role) VALUES
    ('João', 'joao@gmai.com', 'joaopassword', 'COOK'),
    ('Miguel', 'miguel@gmail.com', 'miguelpassword', 'DESK_PAYMENTS'),
    ('José', 'jose@gmail.com', 'josepassword', 'DESK_ORDERS');

do
$$
BEGIN
    for i in 1..15 loop
        INSERT INTO kiosk_terminals DEFAULT VALUES;
    end loop;
END;
$$;

INSERT INTO ingredients (name, price, calories) VALUES
    ('Rice', 0.5, 100),
    ('Beef', 2.5, 200),
    ('Potato', 0.5, 60),
    ('Lettuce', 0.25, 10),
    ('Flour', 0.25, 20),
    ('Egg', 0.5, 100),

INSERT INTO items (name, price) VALUES
    ('Rice with Beef', 3.0),
    ('Potato chips with beef and lettuce', 4.0),
    ('Pancakes', 2.5),
    ('Water', 1.2),
    ('Coke', 4),
    ('Orange Juice', 3);

INSERT INTO main_dishes (id) VALUES (1),(2),(3);
INSERT INTO drinks (id) VALUES (4),(5),(6);

INSERT INTO maindish_ingredients (item_id, ingredient_id, quantity) VALUES
    (1, 1, 4),
    (1, 2, 2),
    (2, 3, 4),
    (2, 2, 2),
    (2, 4, 2),
    (2, 5, 2),
    (2, 6, 2);

INSERT INTO menus (name, image_link) VALUES
    ('Lunch Menu', 'https://cdn.pixabay.com/photo/2015/10/13/21/05/sandwich-986784_960_720.jpg'),
    ('Breakfast Menu', 'https://cdn.pixabay.com/photo/2014/12/15/14/05/salad-569156_960_720.jpg');

INSERT INTO menu_main_dishes (menu_id, item_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3);

INSERT INTO menu_drinks (menu_id, item_id) VALUES
    (1, 4),
    (1, 5),
    (2, 4),
    (2, 6);

INSERT INTO orders (order_status, price, is_paid, is_priority, nif, kiosk_id) VALUES
    ('PICKED_UP', 9.0, true, false, '123456789', 1),
    ('PICKED_UP', 4.0, true, true, '123456787', 2),
    ('PICKED_UP', 6.0, true, false, '123456729', 3),
    ('PICKED_UP', 2.0, true, true, '123456719', 4),
    ('PICKED_UP', 8.0, true, false, '123456489', 5),
    ('PICKED_UP', 9.0, true, false, '123456389', 6);

INSERT INTO order_menus (order_id, menu_id, customization) VALUES
    (1, 1, to_json('{' ||
           'menuId: 1,' ||
           'customizeDrink: {' ||
           'itemId: 7' ||
           '},' ||
           'customizeMainDish: {' ||
           'itemId: 1,' ||
           'customizeIngredients: [{' ||
           'ingredientId: 1,' ||
           'quantity: 3' ||
           '}]' ||
           '}' ||
           '}')
    );
 */




