<?php
// =====================================================================
// FICHERO : ayuda.php
// AUTOR   : Minerva Maravilla Vidaurre
// FECHA   : 05/10/2026
// PROPOSITO: Minicorchesta de pruebas para PHP sin dependencias externas.
//            Sigue el mismo estilo que los tests de JS de la GUI.
// USO      : Sprint 0 - Proyecto Beacon. La usa LogicaNegocioTest.php.
//            No requiere PHPUnit: se ejecuta con "php <test>.php".
// () ------------------------------------------------------------------
// DISENO LOGICO:
//                      ----------------- Ayuda -----------------
//                      |
//         texto: Text --> titulo() -->
//                      |
//                      |
//  real: _, esperado: _ --> igual() -->
//      etiqueta: Text -->
//                      |
//                      |
// condicion: B, etiqueta: Text --> cierto() -->
//                      |
//                      |
//  fn: (...), clase: Text --> lanzaExcepcion() -->
//   etiqueta: Text -->
//                      |
//                      |
//       nombre: Text --> comprobar() -->
//              fn: (...) -->
//                      |
//                      |
//     suite: Text --> resumen() -->
//                      |
//                      ----------------------------------------
// =====================================================================

$GLOBALS['__nComprobaciones'] = 0;
$GLOBALS['__nOk'] = 0;
$GLOBALS['__fallos'] = [];

function titulo(string $texto): void
{
    echo "\n== " . $texto . " ==\n";
}

function igual($real, $esperado, string $etiqueta): void
{
    $GLOBALS['__nComprobaciones']++;
    if ($real === $esperado) {
        $GLOBALS['__nOk']++;
        return;
    }
    $GLOBALS['__fallos'][] = "$etiqueta: se esperaba " . var_export($esperado, true)
        . ", se obtuvo " . var_export($real, true);
}

function cierto(bool $condicion, string $etiqueta): void
{
    $GLOBALS['__nComprobaciones']++;
    if ($condicion) {
        $GLOBALS['__nOk']++;
        return;
    }
    $GLOBALS['__fallos'][] = $etiqueta;
}

function lanzaExcepcion(callable $fn, string $clase, string $etiqueta): void
{
    $GLOBALS['__nComprobaciones']++;
    try {
        $fn();
    } catch (Throwable $e) {
        if ($e instanceof $clase) {
            $GLOBALS['__nOk']++;
            return;
        }
        $GLOBALS['__fallos'][] = "$etiqueta: lanza " . get_class($e)
            . " en vez de $clase: " . $e->getMessage();
        return;
    }
    $GLOBALS['__fallos'][] = "$etiqueta: no se lanzo ninguna excepcion";
}

function comprobar(string $nombre, callable $fn): void
{
    titulo($nombre);
    try {
        $fn();
    } catch (Throwable $e) {
        $GLOBALS['__nComprobaciones']++;
        $GLOBALS['__fallos'][] = "$nombre: fallo no controlado -> " . $e->getMessage();
    }
}

function resumen(string $suite): void
{
    $totales = $GLOBALS['__nComprobaciones'];
    $ok = $GLOBALS['__nOk'];
    echo "\n---- RESUMEN de $suite ----\n";
    if ($GLOBALS['__fallos'] === []) {
        echo "TODAS LAS COMPROBACIONES CORRECTAS\n";
    } else {
        echo "FALLOS:\n";
        foreach ($GLOBALS['__fallos'] as $f) {
            echo "  - " . $f . "\n";
        }
    }
    echo "$ok / $totales comprobaciones correctas\n";
    exit($GLOBALS['__fallos'] === [] ? 0 : 1);
}

// () Fin del fichero.