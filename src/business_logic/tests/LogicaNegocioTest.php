<?php
// =====================================================================
// FICHERO : LogicaNegocioTest.php
// AUTOR   : Minerva Maravilla Vidaurre
// FECHA   : 05/10/2026
// PROPOSITO: Pruebas unitarias e de integracion de la logica de negocio
//            del backend (LogicaNegocio). Las de validacion NO necesitan
//            base de datos; las de integracion usan "mediciones_db_test"
//            y se omiten con un aviso si MySQL no esta disponible.
// USO      : Sprint 0 - Proyecto Beacon.
// Ejecutar : php LogicaNegocioTest.php   (desde esta carpeta)
// () ------------------------------------------------------------------
// DISENO LOGICO:
//                      ----------- LogicaNegocioTest -----------
//                      |
//                      | prepararBBDDPrueba() --> B
//                      |
//                      | $logica: LogicaNegocio
//                      | $hayBBDD: B
//                      |
//                      | A. guardarMedicion() --> InvalidArgumentException
//                      | B. guardarMedicion() --> Medicion
//                      |    recuperarMedicion() --> [ Medicion ]
//                      |
//                      ----------------------------------------
// =====================================================================

require_once __DIR__ . '/ayuda.php';

// ---- Apuntamos las pruebas a una base de datos DIFERENTE a la real ----
// config.php define las constantes solo si no existen: se define BD_NOMBRE
// ANTES de cargar LogicaNegocio (que carga config.php), de modo que
// conectarBD() trabaje sobre "mediciones_db_test" y nunca toque la BBDD
// real "mediciones_db".
if (!defined('BD_NOMBRE')) {
    define('BD_NOMBRE', 'mediciones_db_test');
}

require_once __DIR__ . '/../LogicaNegocio.php';

// ---- Ayudante de integracion: crea y vacia la base de datos de prueba ----
// PROPOSITO: Crea "mediciones_db_test" con la tabla "mediciones" y la vacia,
//            si MySQL esta levantado. Devuelve false si no hay servidor.
// PARAM.   : ninguno.
// RETORNA  : bool - true si la BBDD de prueba esta lista.
// () ------------------------------------------------------------------
function prepararBBDDPrueba(): bool
{
    try {
        $pdo = new PDO(
            'mysql:host=' . BD_SERVIDOR . ';port=' . BD_PUERTO . ';charset=utf8mb4',
            BD_USUARIO,
            BD_CLAVE,
            [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
        );
        $pdo->exec('CREATE DATABASE IF NOT EXISTS mediciones_db_test
                    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        $pdo->exec('USE mediciones_db_test');
        $pdo->exec('DROP TABLE IF EXISTS mediciones');
        $pdo->exec('CREATE TABLE mediciones (
            tipo_medicion       VARCHAR(32)     NULL,
            valor               SMALLINT SIGNED NOT NULL,
            identificador_placa VARCHAR(64)     NOT NULL,
            fecha_hora          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
            PRIMARY KEY (identificador_placa, fecha_hora)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci');
        return true;
    } catch (PDOException $e) {
        return false;
    }
}

$hayBBDD = prepararBBDDPrueba();
if ($hayBBDD) {
    try {
        conectarBD();
    } catch (Throwable $e) {
        $hayBBDD = false;
    }
}
if (!$hayBBDD) {
    echo "\nAVISO: no se encuentra MySQL; las pruebas de integracion se omiten.\n"
       . "  Arranca MySQL (XAMPP) o corrige BD_SERVIDOR/BD_USUARIO/BD_CLAVE.\n";
}

$logica = new LogicaNegocio();

// =====================================================================
// A. VALIDACION SIN BASE DE DATOS
// =====================================================================
comprobar('guardarMedicion lanza si falta "valor"', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion([]);
    }, InvalidArgumentException::class, 'sin campo valor');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => null]);
    }, InvalidArgumentException::class, 'valor null');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => '']);
    }, InvalidArgumentException::class, 'valor vacio');
});

comprobar('guardarMedicion lanza si "valor" no es un entero valido', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 'abc']);
    }, InvalidArgumentException::class, 'texto no numerico');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 12.5]);
    }, InvalidArgumentException::class, 'float no entero');
});

comprobar('guardarMedicion lanza si "valor" esta fuera del rango SMALLINT', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 40000]);
    }, InvalidArgumentException::class, 'por encima del maximo');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => -40000]);
    }, InvalidArgumentException::class, 'por debajo del minimo');
});

comprobar('guardarMedicion lanza si "valor" es cero o negativo', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 0]);
    }, InvalidArgumentException::class, 'valor cero');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => -7]);
    }, InvalidArgumentException::class, 'valor negativo');
});

comprobar('guardarMedicion lanza si "identificador_placa" falta o no es cadena', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 100]);
    }, InvalidArgumentException::class, 'sin identificador_placa');
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 100, 'identificador_placa' => 123]);
    }, InvalidArgumentException::class, 'identificador_placa no es cadena');
});

comprobar('guardarMedicion lanza si "tipo_medicion" no es cadena', function () use ($logica) {
    lanzaExcepcion(function () use ($logica) {
        $logica->guardarMedicion(['valor' => 100, 'tipo_medicion' => ['no es texto']]);
    }, InvalidArgumentException::class, 'tipo_medicion no es cadena');
});

// =====================================================================
// B. INTEGRACION CON LA BASE DE DATOS DE PRUEBA
// =====================================================================
if (!$hayBBDD) {
    echo "\n(pruebas de integracion omitidas: sin MySQL)\n";
} else {
    comprobar('guardarMedicion guarda una medicion valida y devuelve la completa', function () use ($logica) {
        $guardada = $logica->guardarMedicion([
            'valor' => 1234,
            'tipo_medicion' => 'MANUAL',
            'identificador_placa' => 'Minerva_ELBACON',
        ]);
        igual($guardada['valor'], 1234, 'el valor se guardo');
        igual($guardada['tipo_medicion'], 'MANUAL', 'el tipo de magnitud se guardo');
        igual($guardada['identificador_placa'], 'Minerva_ELBACON', 'la placa se guardo');
        cierto(!empty($guardada['fecha_hora']), 'la BBDD genero la fecha_hora');
    });

    comprobar('guardarMedicion admite el tipo ausente (NULL)', function () use ($logica) {
        $guardada = $logica->guardarMedicion([
            'valor' => 5,
            'identificador_placa' => 'PLACA_SIN_TIPO',
        ]);
        igual($guardada['tipo_medicion'], null, 'tipo_medicion NULL');
        igual($guardada['identificador_placa'], 'PLACA_SIN_TIPO', 'la placa se guardo');
    });

    comprobar('recuperarMedicion devuelve [] si no hay mediciones', function () use ($logica) {
        conectarBD()->exec('TRUNCATE TABLE mediciones');
        $lista = $logica->recuperarMedicion();
        cierto(is_array($lista), 'devuelve un array');
        igual(count($lista), 0, 'la lista esta vacia');
    });

    comprobar('recuperarMedicion devuelve todas ordenadas de la mas reciente a la mas antigua', function () use ($logica) {
        conectarBD()->exec('TRUNCATE TABLE mediciones');
        $logica->guardarMedicion(['valor' => 1, 'tipo_medicion' => 'MANUAL',
            'identificador_placa' => 'Minerva_ELBACON']);
        usleep(2000);   // 2 ms: garantiza fecha_hora distinta (precision de milisegundos)
        $logica->guardarMedicion(['valor' => 2, 'tipo_medicion' => 'MANUAL',
            'identificador_placa' => 'Minerva_ELBACON']);
        $lista = $logica->recuperarMedicion();
        igual(count($lista), 2, 'hay dos mediciones');
        igual(array_column($lista, 'valor'), [2, 1], 'la mas reciente aparece primero');
    });
}

resumen('LogicaNegocioTest.php');