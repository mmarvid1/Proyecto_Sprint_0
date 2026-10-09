<?php
// =====================================================================
// FICHERO : ServidorREST.php
// PROPOSITO: Implementa el servidor REST del backend. Expone las 2 rutas
//            del proyecto (POST /mediciones y GET /mediciones) y una
//            tercera ruta de error para todo lo demas.
// USO      : Sprint 0 - Proyecto Beacon. Recibe el POST de la app Android
//            y devuelve el GET con el que la web pinta las mediciones.
// REGLA    : Aqui NO hay ni una sola linea de SQL. Este fichero solo
//            valida el formato de la peticion y delega todo el trabajo
//            en LogicaNegocio.
// =====================================================================

// ---- 1. Cargamos la logica de negocio (unica que habla con la BBDD) ----
require_once __DIR__ . '/../logica/LogicaNegocio.php';

// ---- 2. Clase ServidorREST ----
class ServidorREST
{
    // =================================================================
    // Metodo: guardarMedicion()
    // PROPOSITO: Gestiona el POST /mediciones. Decodifica el cuerpo
    //            JSON, se lo pasa a LogicaNegocio para que lo guarde y
    //            responde con {"ok": true, "id": N}.
    // PARAM.   : string $cuerpo - cuerpo crudo de la peticion HTTP.
    // RETORNA  : nada (emite la respuesta JSON por la salida estandar).
    // () ----------------------------------------------------------------
    public function guardarMedicion(string $cuerpo): void
    {
        // ---- 1. Decodificamos el cuerpo JSON ----
        // null significa que el cuerpo no es JSON valido.
        $datos = json_decode($cuerpo, true);
        if ($datos === null || !is_array($datos)) {
            $this->responder(400, ['error' => 'cuerpo JSON invalido']);
            return;
        }

        // ---- 2. El campo "valor" es obligatorio ----
        // Aqui filtramos el formato; el rango lo valida la logica.
        if (!array_key_exists('valor', $datos)) {
            $this->responder(400, ['error' => "falta el campo 'valor'"]);
            return;
        }

        // ---- 3. Delegamos el guardado en la logica de negocio ----
        try {
            $logica = new LogicaNegocio();
            $medicion = $logica->guardarMedicion($datos);
        } catch (InvalidArgumentException $e) {
            // Datos invalidos (valor vacio, fuera de rango, etc.): 400.
            $this->responder(400, ['error' => $e->getMessage()]);
            return;
        } catch (PDOException $e) {
            // Fallo de BBDD: no es culpa del cliente, es error nuestro.
            $this->responder(500, ['error' => 'error interno del servidor']);
            return;
        }

        // ---- 4. Exito: devolvemos el id de la Medicion guardada ----
        $this->responder(200, ['ok' => true, 'id' => (int) $medicion['id']]);
    }

    // =================================================================
    // Metodo: recuperarMedicion()
    // PROPOSITO: Gestiona el GET /mediciones. Pide a la logica de
    //            negocio todas las mediciones y responde con la lista
    //            completa en formato {"mediciones": [...]}.
    // PARAM.   : ninguno.
    // RETORNA  : nada (emite la respuesta JSON por la salida estandar).
    // () ----------------------------------------------------------------
    public function recuperarMedicion(): void
    {
        try {
            $logica = new LogicaNegocio();
            $mediciones = $logica->recuperarMedicion();
        } catch (PDOException $e) {
            $this->responder(500, ['error' => 'error interno del servidor']);
            return;
        }

        $this->responder(200, ['mediciones' => $mediciones]);
    }

    // =================================================================
    // Metodo: rutaNoEncontrada()
    // PROPOSITO: Responde 404 a cualquier verbo o ruta que no sea
    //            POST /mediciones ni GET /mediciones.
    // PARAM.   : ninguno.
    // RETORNA  : nada (emite la respuesta JSON por la salida estandar).
    // () ----------------------------------------------------------------
    public function rutaNoEncontrada(): void
    {
        $this->responder(404, ['error' => 'ruta no encontrada']);
    }

    // =================================================================
    // Metodo: responder()  (auxiliar, privado)
    // PROPOSITO: Escribe por pantalla el codigo HTTP y el cuerpo JSON
    //            de una respuesta. Es el unico punto que emite salida.
    // PARAM.   : int $codigo - codigo de estado HTTP (400, 404, 500...).
    //            array $cuerpo - datos que se serializan a JSON.
    // RETORNA  : nada.
    // () ----------------------------------------------------------------
    private function responder(int $codigo, array $cuerpo): void
    {
        // ---- Codigo de estado HTTP ----
        http_response_code($codigo);

        // ---- Cabecera de tipo de contenido JSON ----
        if (!headers_sent()) {
            header('Content-Type: application/json; charset=utf-8');
        }

        // ---- Cuerpo JSON ----
        // Con JSON_UNESCAPED_UNICODE los acentos salen legibles y sin
        // barra invertida en las rutas. NO usamos JSON_FORCE_OBJECT
        // porque convertiria las listas vacias [] en {} y el GET sin
        // datos debe responder exactamente {"mediciones":[]}.
        echo json_encode($cuerpo, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }
}