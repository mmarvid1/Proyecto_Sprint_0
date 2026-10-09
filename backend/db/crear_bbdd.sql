-- =====================================================================
-- FICHERO : crear_bbdd.sql
-- PROPOSITO: Crea la base de datos "mediciones_db" con su unica tabla
--            "mediciones" y la deja con 3 filas de ejemplo del beacon,
--            tal y como define el diseño de la base de datos.
-- USO      : Sprint 0 - Proyecto Beacon. Cada medicion captada por la app
--            Android (POST) y validada por la logica de negocio se guarda
--            aqui; la web (GET) las recupera de esta misma tabla.
-- REPARTO  : El valor de la medicion ES el campo "valor" (en este proyecto
--            1234) y el tipo de magnitud medida ES el campo "tipo_medicion".
--            Ya NO existen los campos "uuid" ni "tx_power".
-- =====================================================================

-- ---- 1. Creamos la base de datos ----
-- IF NOT EXISTS: si ya existe no falla y el script continua.
CREATE DATABASE IF NOT EXISTS mediciones_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ---- 2. Seleccionamos la base de datos recien creada ----
USE mediciones_db;

-- ---- 3. Creamos la tabla unica del proyecto ----
-- InnoDB + utf8mb4 como exige el diseno.
-- fecha_hora la pone la propia BBDD (CURRENT_TIMESTAMP), nunca el codigo.
CREATE TABLE IF NOT EXISTS mediciones (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,  -- lo pone la BBDD
    tipo_medicion   VARCHAR(32)     NULL,                      -- metadato opcional: tipo de magnitud
    valor           SMALLINT SIGNED NOT NULL,                  -- OBLIGATORIO: el valor medido
    nombre_emisora  VARCHAR(64)     NULL,                      -- metadato opcional
    fecha_hora      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP, -- la pone la BBDD

    -- La clave primaria es el id (autoincremental).
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---- 4. Insertamos 3 filas de ejemplo del beacon ----
-- Valores reales del proyecto: tipo_medicion 'MANUAL' (medida introducida
-- a mano), valor 1234 y emisora "Minerva_ELBACON". Tres fechas/horas
-- distintas y proximas para que el orden por fecha descendente (que usa
-- recuperarMedicion) sea comprobable.
-- El byte alto de 'major' (MANUAL = 14) es el que identifica el tipo de
-- magnitud; la app lo traduce a este texto antes de mandarlo.
INSERT INTO mediciones
    (tipo_medicion, valor, nombre_emisora, fecha_hora)
VALUES
    ('MANUAL', 1234, 'Minerva_ELBACON', '2026-09-28 09:00:00'),
    ('MANUAL', 1234, 'Minerva_ELBACON', '2026-09-28 09:00:05'),
    ('MANUAL', 1234, 'Minerva_ELBACON', '2026-09-28 09:00:10');

-- () Fin del script.