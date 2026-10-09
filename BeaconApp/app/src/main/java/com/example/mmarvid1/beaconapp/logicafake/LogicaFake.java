package com.example.mmarvid1.beaconapp.logicafake;

import android.util.Log;

import com.example.mmarvid1.beaconapp.Medicion;
import com.example.mmarvid1.beaconapp.PeticionarioREST;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   LogicaFake
// -----------------------------------------------------------------------------------
//                               --------- LogicaFake -----------
//                               |
//                               |
//                               | URL_SERVIDOR: Text   (const)
//                               | ETIQUETA_LOG: Text   (const)
//                               | ETIQUETA_LOG_PRUEBAS: Text   (const)
//                               | elCliente: PeticionarioREST
//                               |
//                               |
//                                LogicaFake() -->
//                               |
//                               |
//  elCliente: PeticionarioREST --> LogicaFake() -->
//                               |
//                               |
//         medicion: Medicion --> guardarMedicion() -->
//  laRespuesta: RespuestaREST -->
//                               |
//                               |
//       nombre_emisora: Text --> construirJSON() <--
//        elTipoMedicion: Text -->
//                elValor: Z -->
//           cuerpoJSON: Text <--
//                               |
//                               --------------------------------------
// -----------------------------------------------------------------------------------

public class LogicaFake {

    public static final String URL_SERVIDOR = "http://192.168.18.178/Proyecto_Sprint0/backend/public/mediciones";
    private static final String ETIQUETA_LOG = ">>>>";
    public static final String ETIQUETA_LOG_PRUEBAS = ">>>>-pruebas";
    private PeticionarioREST elCliente;

// -----------------------------------------------------------------------------------
//                                LogicaFake() -->
// -----------------------------------------------------------------------------------
    public LogicaFake() {
        this(null);
    }

// -----------------------------------------------------------------------------------
//  elCliente: PeticionarioREST --> LogicaFake() -->
// -----------------------------------------------------------------------------------
    public LogicaFake(PeticionarioREST elCliente) {
        this.elCliente = elCliente;
    }

// -----------------------------------------------------------------------------------
//         medicion: Medicion
//  laRespuesta: RespuestaREST
//                          --> guardarMedicion() -->
// -----------------------------------------------------------------------------------
    public void guardarMedicion(Medicion medicion, PeticionarioREST.RespuestaREST laRespuesta) {
        // 1. Log de entrada: se ve que medicion ha entrado y de donde
        Log.d(ETIQUETA_LOG, " guardarMedicion(): me han pasado " + medicion );

        // 2. Montar el JSON con los 3 datos de la medicion
        String cuerpoJSON = construirJSON(medicion.getNombreEmisora(),
                medicion.getTipoMedicion(),
                medicion.getValor());

        Log.d(ETIQUETA_LOG, " guardarMedicion(): JSON=" + cuerpoJSON);
        Log.d(ETIQUETA_LOG, " guardarMedicion(): URL=" + URL_SERVIDOR);

        // 3. Enviarlo con el cliente HTTP que ya existia
        //         Un unico POST. El callback "laRespuesta" es el que recibe el
        //         codigo y el cuerpo de la respuesta, mas adelante y en otro hilo.
        //
        //         El cliente es NUEVO en cada llamada: PeticionarioREST es un
        //         AsyncTask y solo admite una ejecucion por instancia.
        Log.d(ETIQUETA_LOG, " guardarMedicion(): envio el POST ...");

        PeticionarioREST elClienteDeEstaVez = ( this.elCliente != null )
                ? this.elCliente
                : new PeticionarioREST();

        elClienteDeEstaVez.hacerPeticionREST("POST", URL_SERVIDOR, cuerpoJSON, laRespuesta);

        // 4. Log final: aqui solo se ha enviado;
        // lo que conteste el servidor llega despues en "laRespuesta", no ahora.
        Log.d(ETIQUETA_LOG, " guardarMedicion(): POST enviado, esperando la respuesta");

    }

// -----------------------------------------------------------------------------------
//       nombre_emisora: Text
//        elTipoMedicion: Text
//                elValor: Z
//                          --> construirJSON() <--
//           cuerpoJSON: Text <--
//  "tipo_medicion" va como cadena (CO2/TEMPERATURA/RUIDO/MANUAL); si es
//  null se manda null, porque el backend tambien lo admite.
// -----------------------------------------------------------------------------------
    protected String construirJSON(String nombre_emisora, String elTipoMedicion, int elValor) {
        String tipoJSON = ( elTipoMedicion == null )
                ? "null"
                : "\"" + elTipoMedicion + "\"";
        String emisoraJSON = ( nombre_emisora == null )
                ? "null"
                : "\"" + nombre_emisora + "\"";
        return "{ \"tipo_medicion\": " + tipoJSON +
                ", \"valor\": " + elValor +
                ", \"nombre_emisora\": " + emisoraJSON + " }";

    }
}