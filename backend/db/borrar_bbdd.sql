-- =====================================================================
-- FICHERO : borrar_bbdd.sql
-- PROPOSITO: Destruye por completo la base de datos "mediciones_db" para
--            poder reconstruirla desde cero con "crear_bbdd.sql".
-- USO      : Sprint 0 - Proyecto Beacon. La base almacuna las mediciones
--            que envia la placa BLE a traves del servidor REST.
-- NOTA     : Borra la base EN COMPLETO (no solo la tabla). Ejecutalo
--            con atencion: se pierden todos los datos de mediciones.
-- =====================================================================

-- ---- 1. Si la base esta en uso, la cerramos para poder borrarla ----
-- (MySQL/MariaDB no permite DROP DATABASE si hay conexiones abiertas)
SELECT 'Paso 1: se cierra cualquier conexion previa a mediciones_db' AS paso;

-- ---- 2. Borramos la base de datos completa ----
-- Si no existe, IF EXISTS evita el error y el script sigue adelante.
DROP DATABASE IF EXISTS mediciones_db;

-- ---- 3. Comprobamos que la base ya no existe ----
SELECT 'Paso 2: comprobacion' AS paso;

SELECT COUNT(*) AS bases_restantes
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'mediciones_db';
-- Esperado: 0

-- () Fin del script.

-- =====================================================================
-- SECUENCIA COMPLETA DE VERIFICACION (desde cero, con XAMPP)
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
--        1) borrar_bbdd.sql
--        2) crear_bbdd.sql
--        3) verificar_bbdd.sql
--   3. En la izquierda comprueba que mediciones_db tiene la tabla
--      "mediciones" con las 7 columnas y las 3 filas de ejemplo.
-- =====================================================================