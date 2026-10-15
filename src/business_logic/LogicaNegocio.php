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
//            parametros (:valor, :identificador_placa, ...). Nunca se concatena ningun
//            valor dentro del SQL.
// =====================================================================

// ---- 1. Cargamos la configuracion de la base de datos ----
require_once __DIR__ . '/../database/config/config.php';

// ---- 2. Rango de cada campo numerico segun el tipo de la tabla ----
// "valor" es SMALLINT SIGNED. "tipo_medicion" es texto (VARCHAR), asi
// que no tiene rango numerico: se valida como cadena no vacia.
define('LN_RANGE_VALOR', [-32768, 32767]);          // SMALLINT SIGNED

// ---- 3. Clase LogicaNegocio ----
class LogicaNegocio
{
    // -----------------------------------------------------------------
    // Metodo: guardarMedicion()
    // DISENO LOGICO: medicion: Medicion --> guardarMedicion() --> Medicion
    // PROPOSITO: Valida la Medicion recibida, la inserta en la tabla
    //            "mediciones" y devuelve la Medicion ya completa, con
    //            la "fecha_hora" real que puso la BBDD.
    // PARAM.   : array $medicion - datos del POST (valor e
    //            identificador_placa obligatorios, tipo_medicion opcional).
    // RETORNA  : array - la Medicion completa tal como queda en la BBDD.
    // LANZA    : InvalidArgumentException si "valor" o
    //            "identificador_placa" faltan o no son validos.
    // () ----------------------------------------------------------------
    public function guardarMedicion(array $medicion): array
    {
        // ---- 1. Validamos el campo obligatorio "valor" ----
        if (!array_key_exists('valor', $medicion) || $medicion['valor'] === null || $medicion['valor'] === '') {
            throw new InvalidArgumentException('falta valor');
        }

        // "valor" debe ser un entero dentro del rango SMALLINT SIGNED y
        // ser un valor positivo (una medicion valida siempre lo es).
        $valor = $medicion['valor'];
        if (!$this->esEnteroValido($valor) || !$this->estaEnRango($valor, LN_RANGE_VALOR) || $valor <= 0) {
            throw new InvalidArgumentException('valor inválido');
        }
        $valor = (int) $valor;

        // ---- 2. Validamos el campo obligatorio "identificador_placa" ----
        // Es la columna NOT NULL que, junto a fecha_hora, forma la clave
        // primaria compuesta: siempre debe venir con texto no vacio.
        $identificadorPlaca = $this->obligatorioCadena($medicion, 'identificador_placa');

        // ---- 3. Validamos el metadato opcional ----
        // Si viene, debe tener el tipo correcto; si no viene, se guarda
        // como NULL porque la BBDD lo admite nulo.
        // "tipo_medicion" es texto: CO2, TEMPERATURA, RUIDO o MANUAL.
        $tipoMedicion = $this->opcionalCadena($medicion, 'tipo_medicion');

        // ---- 4. Insertamos con sentencia preparada ----
        // "fecha_hora" NO se inserta: la pone la propia BBDD con
        // CURRENT_TIMESTAMP(3), que incluye los milisegundos.
        $pdo = conectarBD();
        $sql = 'INSERT INTO mediciones (tipo_medicion, valor, identificador_placa)
                VALUES (:tipo_medicion, :valor, :identificador_placa)';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute([
            ':tipo_medicion'       => $tipoMedicion,
            ':valor'               => $valor,
            ':identificador_placa' => $identificadorPlaca,
        ]);

        // ---- 5. Construimos y devolvemos la Medicion completa ----
        // Necesitamos la "fecha_hora" real, asi que releemos de la BBDD
        // la ultima fila insertada para esa placa.
        return $this->leerUltimaMedicionDePlaca($identificadorPlaca);
    }

    // -----------------------------------------------------------------
    // Metodo: recuperarMedicion()
    // DISENO LOGICO: recuperarMedicion() --> [ Medicion ]
    // PROPOSITO: Consulta TODAS las mediciones guardadas y devuelve la
    //            lista completa, de la mas reciente a la mas antigua.
    // PARAM.   : ninguno.
    // RETORNA  : array - lista de Mediciones (array vacio si no hay
    //            ninguna; nunca devuelve null).
    // () ----------------------------------------------------------------
    public function recuperarMedicion(): array
    {
        // ---- 1. Consulta preparada de todas las columnas ----
        // Orden descendente por fecha_hora: lo mas reciente aparece
        // primero. La fecha incluye milisegundos, asi que desempata sola.
        $pdo = conectarBD();
        $sql = 'SELECT tipo_medicion, valor, identificador_placa, fecha_hora
                FROM mediciones
                ORDER BY fecha_hora DESC';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute();

        // ---- 2. Devolvemos todas las filas encontradas ----
        // fetchAll devuelve un array vacio (no null) si no hay filas.
        return $sentencia->fetchAll();
    }

    // -----------------------------------------------------------------
    // Metodo: leerUltimaMedicionDePlaca()  (auxiliar, privado)
    // DISENO LOGICO: identificador_placa: Text --> leerUltimaMedicionDePlaca() --> Medicion
    // PROPOSITO: Relee de la BBDD la ultima Medicion insertada para una
    //            placa, para obtener la "fecha_hora" real (con
    //            milisegundos) que se genero al insertarla.
    // PARAM.   : string $identificadorPlaca - placa de la Medicion recien
    //            insertada.
    // RETORNA  : array - la fila completa de la Medicion.
    // () ----------------------------------------------------------------
    private function leerUltimaMedicionDePlaca(string $identificadorPlaca): array
    {
        $pdo = conectarBD();
        $sql = 'SELECT tipo_medicion, valor, identificador_placa, fecha_hora
                FROM mediciones
                WHERE identificador_placa = :identificador_placa
                ORDER BY fecha_hora DESC
                LIMIT 1';
        $sentencia = $pdo->prepare($sql);
        $sentencia->execute([':identificador_placa' => $identificadorPlaca]);

        $fila = $sentencia->fetch();

        // Si por lo que sea no se encuentra, devolvemos lo minimo para
        // que el llamante siempre reciba una Medicion con su placa.
        return $fila === false ? ['identificador_placa' => $identificadorPlaca] : $fila;
    }

    // -----------------------------------------------------------------
    // Metodo: esEnteroValido()  (auxiliar, privado)
    // DISENO LOGICO: valor: Z --> esEnteroValido() --> B
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

    // -----------------------------------------------------------------
    // Metodo: estaEnRango()  (auxiliar, privado)
    // DISENO LOGICO: valor: Z, rango: [ Z ]_2 --> estaEnRango() --> B
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

    // -----------------------------------------------------------------
    // Metodo: opcionalCadena()  (auxiliar, privado)
    // DISENO LOGICO: medicion: Medicion, campo: Text --> opcionalCadena() --> Text|null
    // PROPOSITO: Valida un metadato de texto opcional (tipo_medicion).
    //            Si no viene, devuelve null; si viene, exige que sea una
    //            cadena no vacia.
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

    // -----------------------------------------------------------------
    // Metodo: obligatorioCadena()  (auxiliar, privado)
    // DISENO LOGICO: medicion: Medicion, campo: Text --> obligatorioCadena() --> Text
    // PROPOSITO: Valida un campo de texto obligatorio (identificador_placa).
    //            Debe venir y ser una cadena no vacia; si no, lanza.
    // PARAM.   : array $medicion - datos recibidos.
    //            string $campo - nombre del campo.
    // RETORNA  : string - la cadena validada.
    // LANZA    : InvalidArgumentException si el campo falta o no es texto.
    // () ----------------------------------------------------------------
    private function obligatorioCadena(array $medicion, string $campo): string
    {
        if (!array_key_exists($campo, $medicion) || $medicion[$campo] === null || $medicion[$campo] === '') {
            throw new InvalidArgumentException('falta ' . $campo);
        }
        if (!is_string($medicion[$campo]) || trim($medicion[$campo]) === '') {
            throw new InvalidArgumentException($campo . ' inválido');
        }
        return $medicion[$campo];
    }
}