INSERT INTO tbl_category (name, "description")
VALUES ('Electrónica', 'Dispositivos electrónicos y gadgets'),
       ('Ropa', 'Prendas de vestir para todas las edades'),
       ('Hogar', 'Artículos para el hogar y decoración'),
       ('Deportes', 'Equipamiento y ropa deportiva') ON CONFLICT (name)
  DO
UPDATE SET
    name = EXCLUDED.name,
    "description" = EXCLUDED."description";