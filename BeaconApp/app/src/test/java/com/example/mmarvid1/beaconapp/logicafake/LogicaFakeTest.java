package com.example.mmarvid1.beaconapp.logicafake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// -----------------------------------------------------------------------------------
//  PRUEBAS de LogicaFake (JUnit 4, en la maquina virtual, sin red)
//
//  Que comprueban:
//   1. Que URL_SERVIDOR termina en "/mediciones".
//   2. Que construirJSON() monta el JSON con los valores del proyecto.
//   3. Que los numeros van SIN comillas y SIN ".0" (son enteros, no texto).
//
//  Como se ejecutan:   cd BeaconApp   y   ./gradlew test
// -----------------------------------------------------------------------------------
public class LogicaFakeTest {

    // ------------------------------------------------------------------
    //  Valores de ejemplo del proyecto. Son los mismos que emite el Arduino.
    // ------------------------------------------------------------------
    private static final String UUID_PRUEBA = "EPSG-GTI-PROY-3A";
    private static final String NOMBRE_PRUEBA = "Minerva_ELBACON";
    private static final int MAJOR_PRUEBA = 2816;
    private static final int MINOR_PRUEBA = 1234;
    private static final int TXPOWER_PRUEBA = 4;

    // ------------------------------------------------------------------
    //  PRUEBA 1: la URL del servidor acaba en el recurso "/mediciones".
    // ------------------------------------------------------------------
    @Test
    public void urlServidorTerminaEnMediciones() {

        assertTrue("la URL debe terminar en /mediciones: " + LogicaFake.URL_SERVIDOR,
                LogicaFake.URL_SERVIDOR.endsWith("/mediciones"));
    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 2: el JSON trae los 5 datos con los valores del proyecto.
    // ------------------------------------------------------------------
    @Test
    public void construirJSONTraeTodosLosCampos() {

        LogicaFake laLogica = new LogicaFake(null);

        String elJSON = laLogica.construirJSON(NOMBRE_PRUEBA,
                UUID_PRUEBA,
                MAJOR_PRUEBA,
                MINOR_PRUEBA,
                TXPOWER_PRUEBA);

        // ---- el "minor" que mandamos y que el servidor guarda -------------
        assertTrue("falta \"minor\": 1234  en -> " + elJSON, elJSON.contains("\"minor\": 1234"));

        // ---- el resto de campos del beacon --------------------------------
        assertTrue("falta el uuid en -> " + elJSON, elJSON.contains("\"uuid\": \"" + UUID_PRUEBA + "\""));
        assertTrue("falta \"major\": 2816 en -> " + elJSON, elJSON.contains("\"major\": 2816"));
        assertTrue("falta \"tx_power\": 4 en -> " + elJSON, elJSON.contains("\"tx_power\": 4"));
        assertTrue("falta el nombre_emisora en -> " + elJSON,
                elJSON.contains("\"nombre_emisora\": \"" + NOMBRE_PRUEBA + "\""));

    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 3: los numeros se mandan como enteros (sin comillas y sin ".0").
    // ------------------------------------------------------------------
    @Test
    public void losNumerosSeMandanComoEnteros() {

        LogicaFake laLogica = new LogicaFake(null);

        String elJSON = laLogica.construirJSON(NOMBRE_PRUEBA,
                UUID_PRUEBA,
                MAJOR_PRUEBA,
                MINOR_PRUEBA,
                TXPOWER_PRUEBA);

        // ---- no son decimales (no aparecen como 1234.0, 2816.0 ni 4.0) ----
        assertFalse("minor no puede ir con .0 en -> " + elJSON, elJSON.contains("\"minor\": 1234.0"));
        assertFalse("major no puede ir con .0 en -> " + elJSON, elJSON.contains("\"major\": 2816.0"));
        assertFalse("tx_power no puede ir con .0 en -> " + elJSON, elJSON.contains("\"tx_power\": 4.0"));

        // ---- no van entre comillas (no son texto) -------------------------
        assertFalse("minor no puede ir entre comillas en -> " + elJSON, elJSON.contains("\"minor\": \"1234\""));
        assertFalse("major no puede ir entre comillas en -> " + elJSON, elJSON.contains("\"major\": \"2816\""));
        assertFalse("tx_power no puede ir entre comillas en -> " + elJSON, elJSON.contains("\"tx_power\": \"4\""));

    } // ()

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
