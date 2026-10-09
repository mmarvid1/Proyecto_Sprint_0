package com.example.mmarvid1.beaconapp;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   Medicion
// -----------------------------------------------------------------------------------
//                        --------- Medicion -------------
//                        |
//                        |
//                        | nombreEmisora: Text
//                        | tipoMedicion: Text
//                        | valor: N
//                        |
//                        |
//  nombreEmisora: Text --> Medicion() -->
//          tipoMedicion: Text -->
//            valor: N -->
//                        |
//                        |
//  nombreEmisora: Text --> Medicion() -->
//   tib: TramaIBeacon -->
//                        |
//                        |
//  nombreEmisora: Text <-- getNombreEmisora() <--
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

    private final String nombreEmisora;
    private final String tipoMedicion;
    private final int valor;

// -----------------------------------------------------------------------------------
// nombreEmisora: Text
//          tipoMedicion: Text
//            valor: N
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String nombreEmisora, String tipoMedicion, int valor) {
        this.nombreEmisora = nombreEmisora;
        this.tipoMedicion = tipoMedicion;
        this.valor = valor;
    }

// -----------------------------------------------------------------------------------
//  nombreEmisora: Text
//   tib: TramaIBeacon
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String nombreEmisora, TramaIBeacon tib) {
        this.nombreEmisora = nombreEmisora;
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
//  nombreEmisora: Text <-- getNombreEmisora() <--
// -----------------------------------------------------------------------------------
    public String getNombreEmisora() {
        return this.nombreEmisora;
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
                "nombre_emisora='" + this.nombreEmisora + '\'' +
                ", tipo_medicion='" + this.tipoMedicion + '\'' +
                ", valor=" + this.valor +
                '}';
    }

}
