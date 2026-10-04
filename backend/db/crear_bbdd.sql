-- =====================================================================
-- FICHERO : crear_bbdd.sql
-- PROPOSITO: Crea la base de datos "mediciones_db" con su unica tabla
--            "mediciones" y la deja con 3 filas de ejemplo del beacon,
--            tal y como define el diseño de la base de datos.
-- USO      : Sprint 0 - Proyecto Beacon. Cada medicion captada por la app
--            Android (POST) y validada por la logica de negocio se guarda
--            aqui; la web (GET) las recupera de esta misma tabla.
-- REPARTO  : NO existe el campo "valor_medicion". El valor de la medicion
--            ES el campo "minor" (en este proyecto 1234).
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
    uuid            VARCHAR(32)     NULL,                      -- metadato opcional
    major           SMALLINT SIGNED NULL,                      -- metadato opcional
    minor           SMALLINT SIGNED NOT NULL,                  -- OBLIGATORIO: el valor medido
    tx_power        TINYINT         NULL,                      -- metadato opcional (admite negativos)
    nombre_emisora  VARCHAR(64)     NULL,                      -- metadato opcional
    fecha_hora      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP, -- la pone la BBDD

    -- La clave primaria es el id (autoincremental).
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---- 4. Insertamos 3 filas de ejemplo del beacon ----
-- Valores reales del proyecto: uuid "EPSG-GTI-PROY-3A", minor 1234,
-- major 2816, tx_power 4 y emisora "Minerva_ELBACON".
-- Tres fechas/horas distintas y proximas para que el orden por fecha
-- descendente (que usa recuperarMedicion) sea comprobable.
INSERT INTO mediciones
    (uuid, major, minor, tx_power, nombre_emisora, fecha_hora)
VALUES
    ('EPSG-GTI-PROY-3A', 2816, 1234, 4, 'Minerva_ELBACON', '2026-09-28 09:00:00'),
    ('EPSG-GTI-PROY-3A', 2816, 1234, 4, 'Minerva_ELBACON', '2026-09-28 09:00:05'),
    ('EPSG-GTI-PROY-3A', 2816, 1234, 4, 'Minerva_ELBACON', '2026-09-28 09:00:10');

-- () Fin del script.