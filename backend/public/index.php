<?php
// =====================================================================
// FICHERO : index.php  (front controller del servidor REST)
// PROPOSITO: Punto de entrada unico del backend. Prepara la respuesta
//            JSON, crea el ServidorREST y despacha la peticion hacia el
//            metodo correcto segun el verbo HTTP y la ruta pedida.
// USO      : Sprint 0 - Proyecto Beacon. Gracias al .htaccess, Apache
//            manda aqui TODA peticion, de modo que la ruta bonita es
//            /Proyecto_Sprint0/backend/public/mediciones (sin ".php").
// RUTAS    : POST /mediciones -> ServidorREST::guardarMedicion()
//            GET  /mediciones -> ServidorREST::recuperarMedicion()
//            cualquier otra  -> ServidorREST::rutaNoEncontrada()
// =====================================================================

// ---- 1. Cabecera base: TODAS las respuestas de este backend son JSON ----
header('Content-Type: application/json; charset=utf-8');
// Esta API no se cachea: la web la refresca cada 5 segundos y quiere
// datos siempre frescos.
header('Cache-Control: no-store, no-cache, must-revalidate');

// ---- 2. Cargamos el servidor REST ----
require_once __DIR__ . '/../rest/ServidorREST.php';

// ---- 3. Creamos el servidor ----
$servidor = new ServidorREST();

// ---- 4. Leemos verbo y ruta de la peticion ----
$metodo = isset($_SERVER['REQUEST_METHOD']) ? $_SERVER['REQUEST_METHOD'] : 'GET';
$rutaBruta = isset($_SERVER['REQUEST_URI']) ? $_SERVER['REQUEST_URI'] : '/';
$ruta = parse_url($rutaBruta, PHP_URL_PATH);

// ---- 5. Normalizamos la ruta para quedarse con el ultimo segmento ----
// Con el junction de XAMPP la URL completa es
// /Proyecto_Sprint0/backend/public/mediciones, asi que el recurso es
// siempre el ultimo segmento de la ruta.
$ruta = rtrim($ruta, '/');
$recurso = basename($ruta);

// ---- 6. Despachamos segun verbo HTTP + recurso ----
// POST /mediciones -> guardar la medicion que envia el telefono.
if ($metodo === 'POST' && $recurso === 'mediciones') {
    $servidor->guardarMedicion(file_get_contents('php://input'));
    exit;
}

// GET /mediciones -> devolver todas las mediciones para la web.
if ($metodo === 'GET' && $recurso === 'mediciones') {
    $servidor->recuperarMedicion();
    exit;
}

// ---- 7. Cualquier otra cosa: 404 ----
$servidor->rutaNoEncontrada();
exit;

