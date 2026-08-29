INSERT INTO tbl_category (id, name, "description")
VALUES (1, 'Electrónica', 'Dispositivos electrónicos y gadgets'),
       (2, 'Ropa', 'Prendas de vestir para todas las edades'),
       (3, 'Hogar', 'Artículos para el hogar y decoración'),
       (4, 'Deportes', 'Equipamiento y ropa deportiva') ON CONFLICT (id)
  DO
UPDATE SET
    name = EXCLUDED.name,
    "description" = EXCLUDED."description";