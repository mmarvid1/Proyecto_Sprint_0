<?php
// =====================================================================
// FICHERO : LogicaNegocio.php
// PROPOSITO: Clase con la logica de negocio real del backend. Valida las
//            mediciones y las guarda en la base de datos, y devuelve la
//            lista completa de mediciones guardadas.
// USO      : Sprint 0 - Proyecto Beacon. Es el UNICO puente entre el
//            servidor REST y la base de datos: el servidor REST nunca
//            escribe SQL, solo llama a los 2 metodos de esta clase.
// REGLA    : Todas las consultas usan sentencias preparadas con
//            parametros (:minor, :uuid, ...). Nunca se concatena ningun
//            valor dentro del SQL.
// =====================================================================

// ---- 1. Cargamos la configuracion de la base de datos ----
require_once __DIR__ . '/../config/config.php';

// ---- 2. Rango de cada campo numerico segun el tipo de la tabla ----
define('LN_RANGE_MINOR', [-32768, 32767]);   // SMALLINT SIGNED
define('LN_RANGE_MAJOR', [-32768, 32767]);   // SMALLINT SIGNED
define('LN_RANGE_TX_POWER', [-128, 127]);    // TINYINT (int8)

// ---- 3. Clase LogicaNegocio ----
class LogicaNegocio
{
    // =================================================================
    // Metodo: guardarMedicion()
    // PROPOSITO: Valida la Medicion recibida, la inserta en la tabla
    //            "mediciones" y devuelve la Medicion ya completa, con
    //            el "id" y la "fecha_hora" reales que puso la BBDD.
    // PARAM.   : array $medicion - datos del POST (minor obligatorio,
    //            el resto de metadatos opcionales).
    // RETORNA  : array - la Medicion completa tal como queda en la BBDD.
    // LANZA    : InvalidArgumentException si "minor" falta o no es valido.
    // () ----------------------------------------------------------------
    public function guardarMedicion(array $medicion): array
    {
        // ---- 1. Validamos el campo obligatorio "minor" ----
        if (!array_key_exists('minor', $medicion) || $medicion['minor'] === null || $medicion['minor'] === '') {
            throw new InvalidArgumentException('falta minor');
        }

        // "minor" debe ser un entero dentro del rango SMALLINT SIGNED y
        // ser un valor positivo (una medicion valida siempre lo es).
        $minor = $medicion['minor'];
        if (!$this->esEnteroValido($minor) || !$this->estaEnRango($minor, LN_RANGE_MINOR) || $minor <= 0) {
            throw new InvalidArgumentException('minor inválido');
        }
        $minor = (int) $minor;

        // ---- 2. Validamos los metadatos opcionales ----
        // Si vienen, deben tener el tipo correcto; si no vienen, se
        // guardan como NULL porque la BBDD los admite nulos.
        $major = $this->opcionalEntero($medicion, 'major', LN_RANGE_MAJOR, 'major inválido');
        // tx_power admite negativos: es el int8 de potencia de emision.
        $txPower = $this->opcionalEntero($medicion, 'tx_power', LN_RANGE_TX_POWER, 'tx_power inválido');
        $uuid = $this->opcionalCadena($medicion, 'uuid');
        $nombreEmisora = $this->opcionalCadena($medicion, 'nombre_emisora');

        // ---- 3. Insertamos con sentencia preparada ----
        // "id" y "fecha_hora" NO se insertan: los pone la propia BBDD.
        $pdo = conectarBD();
        $sql = 'INSERT INTO mediciones (uuid, major, minor, tx_power, nombre_emisora)
                VALUES (:uuid, :major, :minor, :tx_power, :nombre_emisora)';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute([
            ':uuid'           => $uuid,
            ':major'          => $major,
            ':minor'          => $minor,
            ':tx_power'       => $txPower,
            ':nombre_emisora' => $nombreEmisora,
        ]);

        // ---- 4. Leemos el "id" generado por la BBDD ----
        $id = (int) $pdo->lastInsertId();

        // ---- 5. Construimos y devolvemos la Medicion completa ----
        // Necesitamos la "fecha_hora" real, asi que la releemos de la BBDD
        return $this->leerMedicionPorId($id);
    }

    // =================================================================
    // Metodo: recuperarMedicion()
    // PROPOSITO: Consulta TODAS las mediciones guardadas y devuelve la
    //            lista completa, de la mas reciente a la mas antigua.
    // PARAM.   : ninguno.
    // RETORNA  : array - lista de Mediciones (array vacio si no hay
    //            ninguna; nunca devuelve null).
    // () ----------------------------------------------------------------
    public function recuperarMedicion(): array
    {
        // ---- 1. Consulta preparada de todas las columnas ----
        // Orden descendente por fecha y, a igualdad de fecha, por id
        // descendente: asi lo mas reciente siempre aparece primero.
        $pdo = conectarBD();
        $sql = 'SELECT id, uuid, major, minor, tx_power, nombre_emisora, fecha_hora
                FROM mediciones
                ORDER BY fecha_hora DESC, id DESC';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute();

        // ---- 2. Devolvemos todas las filas encontradas ----
        // fetchAll devuelve un array vacio (no null) si no hay filas.
        return $sentencia->fetchAll();
    }

    // =================================================================
    // Metodo: leerMedicionPorId()  (auxiliar, privado)
    // PROPOSITO: Relee de la BBDD una Medicion por su id para obtener
    //            la "fecha_hora" real que se genero al insertarla.
    // PARAM.   : int $id - id de la Medicion recien insertada.
    // RETORNA  : array - la fila completa de la Medicion.
    // () ----------------------------------------------------------------
    private function leerMedicionPorId(int $id): array
    {
        $pdo = conectarBD();
        $sql = 'SELECT id, uuid, major, minor, tx_power, nombre_emisora, fecha_hora
                FROM mediciones
                WHERE id = :id';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute([':id' => $id]);

        $fila = $sentencia->fetch();

        // Si por lo que sea no se encuentra, devolvemos lo minimo para
        // que el llamante siempre reciba una Medicion con su id.
        return $fila === false ? ['id' => $id] : $fila;
    }

    // =================================================================
    // Metodo: esEnteroValido()  (auxiliar, privado)
    // PROPOSITO: Dice si un valor recibido por JSON es un entero de verdad.
    // PARAM.   : mixed $valor - valor a comprobar.
    // RETORNA  : bool - true si es entero (o su cadena entera).
    // () ----------------------------------------------------------------
    private function esEnteroValido($valor): bool
    {
        if (is_int($valor)) {
            return true;
        }
        // Aceptamos tambien el numero recibido como cadena, que es el
        // caso tipico cuando el telefono lo manda entrecomillado.
        if (is_string($valor) && preg_match('/^-?\d+$/', trim($valor)) === 1) {
            return true;
        }
        // Un float entero (1234.0) tambien es valido como numero.
        if (is_float($valor) && floor($valor) === $valor) {
            return true;
        }
        return false;
    }

    // =================================================================
    // Metodo: estaEnRango()  (auxiliar, privado)
    // PROPOSITO: Comprueba que un entero cabe en el rango del tipo SQL.
    // PARAM.   : mixed $valor - entero ya validado.
    //            array $rango - [minimo, maximo] del tipo de la columna.
    // RETORNA  : bool - true si el valor esta dentro del rango.
    // () ----------------------------------------------------------------
    private function estaEnRango($valor, array $rango): bool
    {
        $numero = (int) $valor;
        return $numero >= $rango[0] && $numero <= $rango[1];
    }

    // =================================================================
    // Metodo: opcionalEntero()  (auxiliar, privado)
    // PROPOSITO: Valida un metadato numerico opcional. Si no viene,
    //            devuelve null (la BBDD lo admite); si viene, valida
    //            que sea entero y que quepa en el rango de su columna.
    // PARAM.   : array $medicion - datos recibidos.
    //            string $campo - nombre del campo ("major", "tx_power").
    //            array $rango - rango del tipo SQL de esa columna.
    //            string $error - mensaje si el valor no es valido.
    // RETORNA  : int|null - el entero, o null si el campo no viene.
    // () ----------------------------------------------------------------
    private function opcionalEntero(array $medicion, string $campo, array $rango, string $error): ?int
    {
        // El campo no vino: es opcional, se guarda como NULL.
        if (!array_key_exists($campo, $medicion) || $medicion[$campo] === null || $medicion[$campo] === '') {
            return null;
        }
        // Vino: tiene que ser un entero dentro del rango de su columna.
        if (!$this->esEnteroValido($medicion[$campo]) || !$this->estaEnRango($medicion[$campo], $rango)) {
            throw new InvalidArgumentException($error);
        }
        return (int) $medicion[$campo];
    }

    // =================================================================
    // Metodo: opcionalCadena()  (auxiliar, privado)
    // PROPOSITO: Valida un metadato de texto opcional (uuid o
    //            nombre_emisora). Si no viene, devuelve null; si viene,
    //            exige que sea una cadena no vacia.
    // PARAM.   : array $medicion - datos recibidos.
    //            string $campo - nombre del campo.
    // RETORNA  : string|null - la cadena, o null si el campo no viene.
    // () ----------------------------------------------------------------
    private function opcionalCadena(array $medicion, string $campo): ?string
    {
        if (!array_key_exists($campo, $medicion) || $medicion[$campo] === null || $medicion[$campo] === '') {
            return null;
        }
        if (!is_string($medicion[$campo]) || trim($medicion[$campo]) === '') {
            throw new InvalidArgumentException($campo . ' inválido');
        }
        return $medicion[$campo];
    }
}