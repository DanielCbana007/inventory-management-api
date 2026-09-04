INSERT INTO tbl_category (name, "description")
VALUES ('Electrónica', 'Dispositivos electrónicos y gadgets'),
       ('Ropa', 'Prendas de vestir para todas las edades'),
       ('Hogar', 'Artículos para el hogar y decoración'),
       ('Deportes', 'Equipamiento y ropa deportiva')
ON CONFLICT (name) DO NOTHING;

INSERT INTO tbl_product (category_id, "sku", name, "description", price, stock, created_at, updated_at)
VALUES
    ((SELECT id FROM tbl_category WHERE name = 'Electrónica'), 'SKU-0001', 'Laptop Lenovo', 'Laptop 15 pulgadas', 2500000.00, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Electrónica'), 'SKU-0002', 'Mouse Logitech', 'Mouse inalámbrico', 80000.00, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Electrónica'), 'SKU-0006', 'Teclado Mecánico', 'Teclado RGB', 180000.00, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Electrónica'), 'SKU-0007', 'Monitor Samsung', 'Monitor 24 pulgadas', 650000.00, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Electrónica'), 'SKU-0008', 'Audífonos Sony', 'Audífonos bluetooth', 220000.00, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Ropa'), 'SKU-0003', 'Camiseta Nike', 'Camiseta de algodón', 90000.00, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Ropa'), 'SKU-0009', 'Pantalón Jean', 'Pantalón azul', 120000.00, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Ropa'), 'SKU-0010', 'Chaqueta Cuero', 'Chaqueta negra', 350000.00, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Ropa'), 'SKU-0011', 'Zapatillas Puma', 'Zapatillas deportivas', 280000.00, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Hogar'), 'SKU-0004', 'Lámpara LED', 'Lámpara de mesa', 45000.00, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Hogar'), 'SKU-0012', 'Juego de Sábanas', 'Sábanas queen', 95000.00, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Hogar'), 'SKU-0013', 'Set de Ollas', 'Ollas de acero inoxidable', 210000.00, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Hogar'), 'SKU-0014', 'Cortinas Blackout', 'Cortinas para sala', 75000.00, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Deportes'), 'SKU-0005', 'Balón Fútbol', 'Balón tamaño 5', 65000.00, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Deportes'), 'SKU-0015', 'Bicicleta MTB', 'Bicicleta de montaña', 1200000.00, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Deportes'), 'SKU-0016', 'Guantes Boxeo', 'Guantes de 12 onzas', 85000.00, 33, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Deportes'), 'SKU-0017', 'Mancuernas 5kg', 'Par de mancuernas', 130000.00, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM tbl_category WHERE name = 'Deportes'), 'SKU-0018', 'Colchoneta Yoga', 'Colchoneta para yoga', 60000.00, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT ("sku") DO NOTHING;
