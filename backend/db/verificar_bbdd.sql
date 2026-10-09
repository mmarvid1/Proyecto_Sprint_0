-- =====================================================================
-- FICHERO : verificar_bbdd.sql
-- PROPOSITO: Script de prueba de la base de datos "mediciones_db".
--            Comprueba, en orden y con sus comentarios, que la tabla
--            existe, que tiene las 5 columnas del diseño en ese orden,
--            que hay 3 filas de ejemplo y que su contenido es correcto.
-- USO      : Sprint 0 - Proyecto Beacon. Se ejecuta DESPUES de
--            crear_bbdd.sql y sirve para demostrar que la BBDD
--            funciona antes de empezar a desarrollar el backend.
-- ESPERADO : Cada comprobacion debe devolver exactamente lo indicado.
-- =====================================================================

USE mediciones_db;

-- =====================================================================
-- COMPROBACION 1: la tabla "mediciones" existe
-- =====================================================================
-- Esperado: una fila con el nombre de la tabla que acaba de crearse.
SELECT 'COMPROBACION 1: la tabla existe' AS comprobacion;

SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mediciones_db'
  AND TABLE_NAME   = 'mediciones';

-- () Fin comprobacion 1.


-- =====================================================================
-- COMPROBACION 2: existen las 5 columnas, en el orden del diseno
-- =====================================================================
-- Esperado: 5 filas en este orden:
--   1 id | 2 tipo_medicion | 3 valor | 4 nombre_emisora | 5 fecha_hora
-- ORDINAL_POSITION es la posicion real de cada columna dentro de la tabla.
SELECT 'COMPROBACION 2: las 5 columnas en orden' AS comprobacion;

SELECT ORDINAL_POSITION AS posicion,
       COLUMN_NAME     AS columna,
       COLUMN_TYPE     AS tipo,
       IS_NULLABLE     AS admite_nulo,
       COLUMN_KEY      AS clave
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'mediciones_db'
  AND TABLE_NAME   = 'mediciones'
ORDER BY ORDINAL_POSITION;

-- () Fin comprobacion 2.


-- =====================================================================
-- COMPROBACION 3: hay exactamente 3 filas de ejemplo
-- =====================================================================
-- Esperado: total_filas = 3
SELECT 'COMPROBACION 3: numero de filas' AS comprobacion;

SELECT COUNT(*) AS total_filas
FROM mediciones;

-- () Fin comprobacion 3.


-- =====================================================================
-- COMPROBACION 4: el contenido es correcto
-- =====================================================================
-- Muestra TODAS las filas ordenadas de mas reciente a mas antigua, que
-- es el mismo orden que devuelve recuperarMedicion() del backend.
-- Esperado: 3 filas con tipo_medicion = 'MANUAL', valor = 1234
--           y nombre_emisora = Minerva_ELBACON.
-- El CASE anade una comprobacion extra: si valor != 1234 o falta el tipo
-- o la emisora, no debe salir el OK.
SELECT 'COMPROBACION 4: contenido correcto' AS comprobacion;

SELECT id,
       tipo_medicion,
       valor,
       nombre_emisora,
       fecha_hora
FROM mediciones
ORDER BY fecha_hora DESC, id DESC;

SELECT CASE
         WHEN COUNT(*) = 3 THEN 'OK: las 3 filas tienen los valores correctos'
         ELSE 'ERROR: hay filas con valores incorrectos'
       END AS valoracion,
       COUNT(*) AS filas_correctas
FROM mediciones
WHERE tipo_medicion  = 'MANUAL'
  AND valor          = 1234
  AND nombre_emisora = 'Minerva_ELBACON';

-- () Fin comprobacion 4.


-- =====================================================================
-- SECUENCIA COMPLETA DE VERIFICACION (reconstruir la BBDD desde cero)
-- ---------------------------------------------------------------------
-- Opcion A - Consola (cliente MySQL de XAMPP, en C:\xampp\mysql\bin,
--             en esta maquina en M:\xampp\mysql\bin):
--
--   mysql -u root < backend/db/borrar_bbdd.sql
--   mysql -u root < backend/db/crear_bbdd.sql
--   mysql -u root < backend/db/verificar_bbdd.sql
--
-- Opcion B - phpMyAdmin:
--   1. Abre http://localhost/phpmyadmin
--   2. Pestana "Importar" y ejecuta, EN ESTE ORDEN:
--        1) borrar_bbdd.sql     -> desaparece mediciones_db
--        2) crear_bbdd.sql      -> se crea mediciones_db.mediciones
--        3) verificar_bbdd.sql  -> las 4 comprobaciones deben pasar
--   3. En la izquierda comprueba que mediciones_db tiene la tabla
--      "mediciones" con las 5 columnas y las 3 filas.
--
-- Resultado esperado de verificar_bbdd.sql:
--   COMPROBACION 1 -> mediciones
--   COMPROBACION 2 -> 5 filas en el orden del diseno
--   COMPROBACION 3 -> total_filas = 3
--   COMPROBACION 4 -> 3 filas, de mas reciente a mas antigua,
--                     valoracion = "OK: las 3 filas tienen los valores correctos"
-- =====================================================================