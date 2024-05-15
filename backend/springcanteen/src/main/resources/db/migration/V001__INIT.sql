CREATE TYPE Employee_role AS ENUM('COOK', 'DESK_PAYMENTS', 'DESK_ORDERS');
CREATE TYPE Order_status AS ENUM('IDLE', 'PREPARING', 'READY', 'PICKED_UP');

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
    price numeric(5, 2) not null,
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
    is_paid boolean not null,
    is_priority boolean not null,
    nif varchar(9) not null,
    kiosk_id integer not null
);

CREATE TABLE order_menus(
    id integer PRIMARY KEY,
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
    ('Bread', 0.5, 100),
    ('Wrap', 0.4, 30),
    ('Cheese', 1.5, 200),
    ('Ham', 2.5, 300),
    ('Egg', 1.0, 150),
    ('Tomato', 0.5, 50),
    ('Lettuce', 0.5, 20),
    ('Beef', 3.0, 400),
    ('Chicken', 2.5, 350),
    ('Pork', 2.0, 300),
    ('Fish', 3.5, 250),
    ('Yogurt', 2.5, 59),
    ('Banana', 2.5, 59);

INSERT INTO items (name, price) VALUES
    ('Sandwich', 3.0),
    ('Russian Salad', 4.0),
    ('Yogurt', 2.5),
    ('Veggie Wrap', 3.5),
    ('Chicken Salad', 5.0),
    ('Yogurt With Banana', 4),
    ('Orange Juice', 3),
    ('Lemonade', 2),
    ('Water', 1.2);

INSERT INTO main_dishes (id) VALUES (1),(2),(3),(4),(5),(6);
INSERT INTO drinks (id) VALUES (7),(8),(9);

INSERT INTO maindish_ingredients (item_id, ingredient_id, quantity) VALUES
    (1, 1, 1),
    (1, 3, 2),
    (1, 4, 1),
    (2, 3, 1),
    (2, 5, 1),
    (2, 6, 1),
    (2, 7, 1),
    (3, 12, 1),
    (4, 2, 1),
    (4, 3, 1),
    (4, 6, 1),
    (4, 7, 1),
    (5, 5, 1),
    (5, 6, 1),
    (5, 7, 1),
    (5, 9, 2),
    (6, 12, 1),
    (6, 13, 1);

INSERT INTO menus (name, price, image_link) VALUES
    ('Sandwich & Drink', 5.0, 'https://cdn.pixabay.com/photo/2015/10/13/21/05/sandwich-986784_960_720.jpg'),
    ('Russian Salad & Water', 6.0, 'https://cdn.pixabay.com/photo/2014/12/15/14/05/salad-569156_960_720.jpg'),
    ('Yogurt with banana', 5.4, 'https://cdn.pixabay.com/photo/2023/04/14/14/30/oatmeal-7925232_960_720.jpg'),
    ('Veggie Wrap', 8.0, 'https://cdn.pixabay.com/photo/2023/09/27/12/11/wine-8279458_960_720.jpg'),
    ('Chicken Salad & Drink', 9.0, 'https://cdn.pixabay.com/photo/2019/03/14/19/03/chicken-4055653_960_720.jpg');

INSERT INTO menu_main_dishes (menu_id, item_id) VALUES
    (1, 1),
    (2, 2),
    (3, 6),
    (4, 4),
    (5, 5);

INSERT INTO menu_drinks (menu_id, item_id) VALUES
    (1, 7),
    (1, 8),
    (1, 9),
    (2, 9),
    (4, 7),
    (4, 8),
    (4, 9),
    (5, 7),
    (5, 8),
    (5, 9);

INSERT INTO orders (order_status, is_paid, is_priority, nif, kiosk_id) VALUES
    ('PICKED_UP', true, false, '123456789', 1),
    ('PICKED_UP', true, true, '123456787', 2),
    ('PICKED_UP', true, false, '123456729', 3),
    ('PICKED_UP', true, true, '123456719', 4),
    ('PICKED_UP', true, false, '123456489', 5),
    ('PICKED_UP', true, false, '123456389', 6);

INSERT INTO order_menus (id, menu_id, customization) VALUES
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





