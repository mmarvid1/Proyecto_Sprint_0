package com.example.mmarvid1.beaconapp.logicafake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   CLASE COMPLETA
// -----------------------------------------------------------------------------------
//                        --------- LogicaFakeTest -------
//                        |
//                        |
//                        | // Pruebas JUnit 4 en la maquina virtual, sin red.
//                        | // Cada metodo devuelve B: true = la comprobacion
//                        | // se cumple.
//                        |
//                        | NOMBRE_PRUEBA: Text     (const)
//                        | TIPOMEDICION_PRUEBA: Text   (const)
//                        | VALOR_PRUEBA: N   (const)
//                        |
//                        |
//                   B <-- urlServidorTerminaEnMediciones() <--
//                        |
//                        |
//                   B <-- construirJSONTraeTodosLosCampos() <--
//                        |
//                        |
//                   B <-- losNumerosSeMandanComoEnteros() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  PRUEBAS de LogicaFake (JUnit 4, en la maquina virtual, sin red)
//
//  Que comprueban:
//   1. Que URL_SERVIDOR termina en "/mediciones".
//   2. Que construirJSON() monta el JSON con los valores del proyecto.
//   3. Que "valor" va SIN comillas y SIN ".0" (es un entero, no texto).
//
//  Como se ejecutan:   cd BeaconApp   y   ./gradlew test
// -----------------------------------------------------------------------------------
public class LogicaFakeTest {

    // ------------------------------------------------------------------
    //  Valores de ejemplo del proyecto. Son los mismos que emite el Arduino.
    // ------------------------------------------------------------------
    private static final String NOMBRE_PRUEBA = "Minerva_ELBACON";
    private static final String TIPOMEDICION_PRUEBA = "MANUAL";
    private static final int VALOR_PRUEBA = 1234;

    // ------------------------------------------------------------------
    //  PRUEBA 1: la URL del servidor acaba en el recurso "/mediciones".
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                        --------- LogicaFakeTest -------
//                        |
//                        |
//                        |
//                   B <-- urlServidorTerminaEnMediciones() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void urlServidorTerminaEnMediciones() {

        assertTrue("la URL debe terminar en /mediciones: " + LogicaFake.URL_SERVIDOR,
                LogicaFake.URL_SERVIDOR.endsWith("/mediciones"));
    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 2: el JSON trae los 3 datos con los valores del proyecto.
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                        --------- LogicaFakeTest -------
//                        |
//                        |
//                        |
//                   B <-- construirJSONTraeTodosLosCampos() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void construirJSONTraeTodosLosCampos() {

        LogicaFake laLogica = new LogicaFake(null);

        String elJSON = laLogica.construirJSON(NOMBRE_PRUEBA,
                TIPOMEDICION_PRUEBA,
                VALOR_PRUEBA);

        // ---- el "valor" que mandamos y que el servidor guarda -------------
        assertTrue("falta \"valor\": 1234  en -> " + elJSON, elJSON.contains("\"valor\": 1234"));

        // ---- el resto de campos de la medicion ----------------------------
        assertTrue("falta \"tipo_medicion\": \"MANUAL\" en -> " + elJSON,
                elJSON.contains("\"tipo_medicion\": \"" + TIPOMEDICION_PRUEBA + "\""));
        assertTrue("falta el nombre_emisora en -> " + elJSON,
                elJSON.contains("\"nombre_emisora\": \"" + NOMBRE_PRUEBA + "\""));

    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 3: los numeros se mandan como enteros (sin comillas y sin ".0").
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                        --------- LogicaFakeTest -------
//                        |
//                        |
//                        |
//                   B <-- losNumerosSeMandanComoEnteros() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void losNumerosSeMandanComoEnteros() {

        LogicaFake laLogica = new LogicaFake(null);

        String elJSON = laLogica.construirJSON(NOMBRE_PRUEBA,
                TIPOMEDICION_PRUEBA,
                VALOR_PRUEBA);

        // ---- "valor" no es decimal (no aparece como 1234.0) ---------------
        assertFalse("valor no puede ir con .0 en -> " + elJSON, elJSON.contains("\"valor\": 1234.0"));

        // ---- "valor" no va entre comillas (es un numero, no texto) --------
        assertFalse("valor no puede ir entre comillas en -> " + elJSON, elJSON.contains("\"valor\": \"1234\""));

    } // ()

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
