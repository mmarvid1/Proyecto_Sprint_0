package com.example.mmarvid1.beaconapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   CLASE COMPLETA
// -----------------------------------------------------------------------------------
//                    --------- MedicionTest ---------
//                    |
//                    |
//                    | // Pruebas JUnit 4 en la maquina virtual.
//                    | // Sin Bluetooth y sin red. Cada metodo devuelve
//                    | // B: true = la comprobacion se cumple.
//                    |
//                    |
//                    |
//               B <-- elMinorSeLeeEnElOrdenEnQueViene() <--
//                    |
//                    |
//               B <-- elMajorTambienVaEnOrdenNormal() <--
//                    |
//                    |
//               B <-- elTxPowerRespetaElSigno() <--
//                    |
//                    |
//               B <-- unaMedicionCompletaSeLeeBien() <--
//                    |
//                    --------------------------------------
// -----------------------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  PRUEBAS de la conversion de bytes a enteros y de la clase Medicion
//  (JUnit 4, en la maquina virtual, sin Bluetooth ni sin red)
//
//  Que comprueban:
//   1. Que Utilidades.bytesToIntOK() lee los bytes en ORDEN NORMAL, que es como
//      los manda la libreria BLEBeacon de Adafruit (esa hace __swap16 a proposito).
//   2. Que el "txPower", que es un byte CON signo, sale negativo cuando toca.
//   3. Que una Medicion construida desde una trama iBeacon entera da los valores
//      que emite el Arduino: minor 1234 y tx_power 4.
//
//  Como se ejecutan:   cd BeaconApp   y   ./gradlew test
// -----------------------------------------------------------------------------------
public class MedicionTest {

    // ------------------------------------------------------------------
    //  PRUEBA 1: el minor 1234 = 0x04D2 viaja como 0x04 0xD2, en orden
    //  normal. No hay que invertirlo.
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                    --------- MedicionTest ---------
//                    |
//                    |
//                    |
//               B <-- elMinorSeLeeEnElOrdenEnQueViene() <--
//                    |
//                    --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void elMinorSeLeeEnElOrdenEnQueViene() {

        byte[] losBytesDelMinor = { (byte) 0x04, (byte) 0xD2 };

        assertEquals(1234, Utilidades.bytesToIntOK(losBytesDelMinor));
    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 2: el major. El Arduino manda 0x0B00 + contador, asi que con
    //  contador = 163 sale 0x0BA3 = 2979, y en el aire van 0x0B 0xA3.
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                    --------- MedicionTest ---------
//                    |
//                    |
//                    |
//               B <-- elMajorTambienVaEnOrdenNormal() <--
//                    |
//                    --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void elMajorTambienVaEnOrdenNormal() {

        byte[] losBytesDelMajor = { (byte) 0x0B, (byte) 0xA3 };

        assertEquals(2979, Utilidades.bytesToIntOK(losBytesDelMajor));
    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 3: el txPower es un byte CON signo, asi que 0xC5 es -59 y
    //  no 197.
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                    --------- MedicionTest ---------
//                    |
//                    |
//                    |
//               B <-- elTxPowerRespetaElSigno() <--
//                    |
//                    --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void elTxPowerRespetaElSigno() {

        assertEquals(4, Utilidades.bytesToIntOK(new byte[]{ (byte) 0x04 }));
        assertEquals(-59, Utilidades.bytesToIntOK(new byte[]{ (byte) 0xC5 }));
    } // ()

    // ------------------------------------------------------------------
    //  PRUEBA 4: una medicion completa, montada con los mismos bytes que
    //  emite el Arduino, sale con los valores esperados.
    // ------------------------------------------------------------------

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                    --------- MedicionTest ---------
//                    |
//                    |
//                    |
//               B <-- unaMedicionCompletaSeLeeBien() <--
//                    |
//                    --------------------------------------
// -----------------------------------------------------------------------------------
    @Test
    public void unaMedicionCompletaSeLeeBien() {

        // ---- los 30 bytes de una trama iBeacon tal cual las monta el Arduino ----
        String elUuid = "EPSG-GTI-PROY-3A";

        byte[] laTrama = new byte[30];

        laTrama[0] = 0x02; laTrama[1] = 0x01; laTrama[2] = 0x06; // advFlags
        laTrama[3] = 0x1A; laTrama[4] = (byte) 0xFF;             // advHeader
        laTrama[5] = 0x4C; laTrama[6] = 0x00;                   // companyID (Apple)
        laTrama[7] = 0x02;                                       // iBeacon type
        laTrama[8] = 0x15;                                       // longitud = 21

        for (int i = 0; i < elUuid.length(); i++) {              // uuid, 16 bytes
            laTrama[9 + i] = (byte) elUuid.charAt(i);
        }

        laTrama[25] = 0x0B; laTrama[26] = (byte) 0xA3;           // major = 0x0BA3
        laTrama[27] = 0x04; laTrama[28] = (byte) 0xD2;           // minor = 0x04D2
        laTrama[29] = (byte) 0x04;                              // txPower = 4

        // ---- la clase que lo parte en campos ----
        TramaIBeacon tib = new TramaIBeacon(laTrama);

        // ---- y la medicion, que convierte los bytes a enteros ----
        Medicion laMedicion = new Medicion( "Minerva_ELBACON", tib );

        assertEquals("Minerva_ELBACON", laMedicion.getNombreEmisora());
        assertEquals(elUuid, laMedicion.getUuid());
        assertEquals(2979, laMedicion.getMajor());
        assertEquals(1234, laMedicion.getMinor());
        assertEquals(4, laMedicion.getTxPower());
    } // ()

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
