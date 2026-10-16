package com.example.mmarvid1.beaconapp;

// ===========================================================================
//  FICHERO   : Medicion.java
//  PROPOSITO : Modelo de una medicion (placa, tipo y valor) y su lectura desde una trama iBeacon.
//  AUTOR     : Minerva Maravilla Vidaurre
//  FECHA     : 05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   Medicion
// -----------------------------------------------------------------------------------
//                        --------- Medicion -------------
//                        |
//                        |
//                        | identificadorPlaca: Text
//                        | tipoMedicion: Text
//                        | valor: N
//                        |
//                        |
//  identificadorPlaca: Text --> Medicion() -->
//          tipoMedicion: Text -->
//            valor: N -->
//                        |
//                        |
//  identificadorPlaca: Text --> Medicion() -->
//   tib: TramaIBeacon -->
//                        |
//                        |
//  identificadorPlaca: Text <-- getIdentificadorPlaca() <--
//                        |
//                        |
//          tipoMedicion: Text <-- getTipoMedicion() <--
//                        |
//                        |
//            valor: N <-- getValor() <--
//                        |
//                        |
//      medicion: Text <-- toString() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------

public class Medicion {

    // Los mismos valores que el enum MedicionesID del Arduino. El tipo de
    // magnitud viaja en el byte alto de "major"; aqui se traduce a texto.
    private static final int CO2 = 11;
    private static final int TEMPERATURA = 12;
    private static final int RUIDO = 13;
    private static final int MANUAL = 14;

    private final String identificadorPlaca;
    private final String tipoMedicion;
    private final int valor;

// -----------------------------------------------------------------------------------
// identificadorPlaca: Text
//          tipoMedicion: Text
//            valor: N
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String identificadorPlaca, String tipoMedicion, int valor) {
        this.identificadorPlaca = identificadorPlaca;
        this.tipoMedicion = tipoMedicion;
        this.valor = valor;
    }

// -----------------------------------------------------------------------------------
//  identificadorPlaca: Text
//   tib: TramaIBeacon
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String identificadorPlaca, TramaIBeacon tib) {
        this.identificadorPlaca = identificadorPlaca;
        // major = (tipo << 8) + contador, asi que el tipo es "major >> 8".
        int elTipo = Utilidades.bytesToIntOK( tib.getMajor() ) >> 8;
        this.tipoMedicion = tipoMedicionATexto( elTipo );
        this.valor = Utilidades.bytesToIntOK( tib.getMinor() );
    }

// -----------------------------------------------------------------------------------
//          tipo: N <-- tipoMedicionATexto() <--
//          texto: Text <--
//  Traduce el numero de magnitud (byte alto de major) al texto que se
//  guarda en la BBDD. Devuelve null si el tipo es desconocido.
// -----------------------------------------------------------------------------------
    private static String tipoMedicionATexto(int tipo) {
        switch (tipo) {
            case CO2:        return "CO2";
            case TEMPERATURA: return "TEMPERATURA";
            case RUIDO:      return "RUIDO";
            case MANUAL:     return "MANUAL";
            default:         return null;
        }
    }

// -----------------------------------------------------------------------------------
//  identificadorPlaca: Text <-- getIdentificadorPlaca() <--
// -----------------------------------------------------------------------------------
    public String getIdentificadorPlaca() {
        return this.identificadorPlaca;
    }

// -----------------------------------------------------------------------------------
//          tipoMedicion: Text <-- getTipoMedicion() <--
// -----------------------------------------------------------------------------------
    public String getTipoMedicion() {
        return this.tipoMedicion;
    }

// -----------------------------------------------------------------------------------
//            valor: N <-- getValor() <--
// -----------------------------------------------------------------------------------
    public int getValor() {
        return this.valor;
    }

// -----------------------------------------------------------------------------------
//      medicion: Text <-- toString() <--
// -----------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "Medicion{" +
                "identificador_placa='" + this.identificadorPlaca + '\'' +
                ", tipo_medicion='" + this.tipoMedicion + '\'' +
                ", valor=" + this.valor +
                '}';
    }

}
