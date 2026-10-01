package com.example.mmarvid1.beaconapp;

// -----------------------------------------------------------------------------------
//  CLASE: Constantes
//
//  Que hace:.guarda en un solo sitio los valores fijos del proyecto, para no
//            repetirlos (ni equivocarse) en varias clases.
//
//  Valores:
//    NOMBRE_EMISORA        -> el nombre que pone el Arduino en el scan response.
//                             Tiene que ser EXACTAMENTE el mismo que en
//                             MandarBeacon_Arduino -> Publicador.h.
//    INTERVALO_MINIMO_POST -> cada cuanto se puede mandar una medicion al
//                             servidor. El beacon se ve decenas de veces por
//                             segundo, asi que sin este tope se saturaria el
//                             servidor de peticiones.
// -----------------------------------------------------------------------------------
public class Constantes {

    // ------------------------------------------------------------------
    //  Nombre de la emisora que buscamos.
    //
    //  Va en el SCAN RESPONSE del beacon, no en la trama iBeacon, asi que
    //  para leerlo hay que mirar resultado.getScanRecord().getDeviceName()
    //  (NO resultado.getDevice().getName(), que necesita BLUETOOTH_CONNECT y
    //  solo devuelve el nombre cacheado del GAP).
    // ------------------------------------------------------------------
    public static final String NOMBRE_EMISORA = "Minerva_ELBACON";

    // ------------------------------------------------------------------
    //  Separacion minima entre dos POST seguidos, en milisegundos.
    // ------------------------------------------------------------------
    public static final long INTERVALO_MINIMO_POST = 5000;

    // ------------------------------------------------------------------
    //  Constructor privado: esta clase no se instancia.
    // ------------------------------------------------------------------
    private Constantes() {
    }

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
