<?php
// =====================================================================
// FICHERO : config.php
// PROPOSITO: Centraliza los datos de conexion con la base de datos y
//            ofrece conectarBD() para abrir la conexion PDO.
// USO      : Sprint 0 - Proyecto Beacon. Lo usa LogicaNegocio, que es
//            el unico punto del backend que habla con la BBDD.
// NOTA     : Las constantes se definen SOLO si no existen todavia, para
//            que las pruebas puedan apuntar a "mediciones_db_test"
//            definiendolas antes de cargar este fichero, sin tocar la
//            configuracion real.
// =====================================================================

// ---- 1. Constantes de conexion ----
if (!defined('BD_SERVIDOR')) {
    define('BD_SERVIDOR', 'localhost');
}
if (!defined('BD_NOMBRE')) {
    define('BD_NOMBRE', 'mediciones_db');
}
if (!defined('BD_USUARIO')) {
    define('BD_USUARIO', 'root');
}
if (!defined('BD_CLAVE')) {
    define('BD_CLAVE', '');
}
if (!defined('BD_PUERTO')) {
    define('BD_PUERTO', '3306');
}

// ---- 2. Funcion conectarBD ----
// PROPOSITO: Abre y devuelve una conexion PDO a la base de datos del
//            proyecto configurada con las constantes de arriba.
// PARAM.   : ninguno.
// RETORNA  : PDO - conexion lista para usar con sentencias preparadas.
// () ------------------------------------------------------------------
function conectarBD(): PDO
{
    // ---- Montamos el DSN con los datos de configuracion ----
    $dsn = 'mysql:host=' . BD_SERVIDOR
         . ';port=' . BD_PUERTO
         . ';dbname=' . BD_NOMBRE
         . ';charset=utf8mb4';

    // ---- Opciones de PDO: errores como excepciones y fetch a array ----
    $opciones = [
        // Con esto un fallo de SQL lanza PDOException y no pasa
        // silenciosamente, que es justo lo que espera el servidor REST.
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        // Devuelve cada fila como array asociativo (nombre de columna => valor).
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        // false = usa sentencias preparadas reales en el servidor MySQL.
        PDO::ATTR_EMULATE_PREPARES   => false,
    ];

    // ---- Abrimos la conexion ----
    $pdo = new PDO($dsn, BD_USUARIO, BD_CLAVE, $opciones);

    return $pdo;
}

// () Fin del fichero.