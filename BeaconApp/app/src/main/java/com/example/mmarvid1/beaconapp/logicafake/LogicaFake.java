package com.example.mmarvid1.beaconapp.logicafake;

import android.util.Log;

import com.example.mmarvid1.beaconapp.Medicion;
import com.example.mmarvid1.beaconapp.PeticionarioREST;

// -----------------------------------------------------------------------------------
//  CLASE: LogicaFake
//
//  Que hace: recibe la medicion que acaba de leer el beacon y la manda por POST
//            al servidor REST del proyecto.
//
//  A que objetivo del proyecto sirve:
//            Es el puente entre el mundo fisico y la base de datos. El Arduino
//            emite un iBeacon (UUID, major, minor, txPower) mas su nombre en el
//            scan response, la app lo detecta, arma un objeto Medicion y aqui se
//            convierte en el JSON que el servidor espera:
//
//                { "minor": 1234,
//                  "uuid": "EPSG-GTI-PROY-3A",
//                  "major": 2817,
//                  "tx_power": 4,
//                  "nombre_emisora": "Minerva_ELBACON" }
//
//            El metodo NO devuelve nada: el dato se va por el POST (efecto
//            lateral) y la confirmacion del servidor vuelve por el callback
//            "laRespuesta", de forma asincrona, nunca como valor de retorno.
//
//  Estilo: comentarios en español, separadores "// ----", tag de log ">>>>".
// -----------------------------------------------------------------------------------
public class LogicaFake {

    // ------------------------------------------------------------------
    //  URL_SERVIDOR : Text (const)
    //
    //  La ruta canonica del recurso. Termina en "/mediciones" por diseno: es el
    //  nombre del recurso, y el .htaccess de Apache lo sirve sin ".php".
    //
    //  OJO CON LA IP: aqui va la de este PC, que cambia segun la red. Si la
    //  cambias tambien en res/values/strings.xml (url_servidor), las dos cosas
    //  tienen que apuntar al mismo sitio. En el emulador, la IP del PC es
    //  10.0.2.2 y no la real.
    // ------------------------------------------------------------------
    public static final String URL_SERVIDOR =
            "http://192.168.18.115/Proyecto_Sprint0/backend/public/mediciones";

    // ------------------------------------------------------------------
    //  Etiqueta de los logs de esta clase.
    // ------------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    // ------------------------------------------------------------------
    //  Etiquetas de los logs de las pruebas automaticas (/src/test/...).
    // ------------------------------------------------------------------
    public static final String ETIQUETA_LOG_PRUEBAS = ">>>>-pruebas";

    // ------------------------------------------------------------------
    //  elCliente: PeticionarioREST
    //
    //  OJO, esto es importante. PeticionarioREST hereda de AsyncTask, y un
    //  AsyncTask SOLO se puede ejecutar UNA vez: si se vuelve a llamar a su
    //  execute() lanza "IllegalStateException: Cannot execute task: the task
    //  is already running" y closes la app.
    //
    //  Por eso este atributo vale null en el uso normal, y cada POST crea su
    //  propio PeticionarioREST. Solo se rellena cuando se inyecta uno desde
    //  fuera (las pruebas), y entonces solo se puede usar para UNA peticion.
    // ------------------------------------------------------------------
    private PeticionarioREST elCliente;

    // ------------------------------------------------------------------
    //  Constructor de la clase LogicaFake
    //
    //  PROPOSITO : dejar listo el cliente REST con el que se enviaran los datos.
    //  PARAM.    : ninguno.
    //  RETORNA   : nada (es un constructor).
    //  NOTA      : PeticionarioREST es la clase que YA venia en el esqueleto; no
    //              se ha tocado su codigo.
    // ------------------------------------------------------------------
    public LogicaFake() {
        this(null);
    } // ()

    // ------------------------------------------------------------------
    //  Constructor de la clase LogicaFake (con cliente)
    //
    //  PROPOSITO : poder inyectar otro cliente. Lo usan las pruebas, que no
    //              quieren salir a la red de verdad.
    //  PARAM.    : elCliente -> el PeticionarioREST a usar.
    //  RETORNA   : nada (es un constructor).
    //  NOTA      : en la app se usa el constructor sin parametros.
    // ------------------------------------------------------------------
    public LogicaFake(PeticionarioREST elCliente) {
        this.elCliente = elCliente;
    } // ()

    // ------------------------------------------------------------------
    //  guardarMedicion()
    //
    //  PROPOSITO : enviar por POST al servidor REST una medicion ya construida.
    //              No devuelve nada.
    //  PARAM.    : medicion    -> el objeto con los 5 datos del beacon, ya
    //                             convertidos a enteros por la clase Medicion.
    //              laRespuesta -> callback donde llega la respuesta del
    //                             servidor (PeticionarioREST.RespuestaREST).
    //  RETORNA   : nada (void). El exito se conoce en "laRespuesta": si el
    //              servidor contesta codigo == 200, la medicion quedo guardada.
    //  NOTA      : aqui NO se convierten bytes. Eso ya lo ha hecho Medicion al
    //              construirse desde la trama, que es donde se sabe en que
    //              orden vienen los bytes (little-endian). Si se hiciera aqui
    //              con BigInteger, el minor 1234 (0xD2 0x04) saldria -11772.
    // ------------------------------------------------------------------
    public void guardarMedicion(Medicion medicion,
                                PeticionarioREST.RespuestaREST laRespuesta) {

        // ---- 1. Log de entrada: se ve que medicion ha entrado y de donde --------
        Log.d(ETIQUETA_LOG, " guardarMedicion(): me han pasado " + medicion );

        // ---- 2. Montar el JSON con los 5 datos de la medicion -------------------
        String cuerpoJSON = construirJSON(medicion.getNombreEmisora(),
                medicion.getUuid(),
                medicion.getMajor(),
                medicion.getMinor(),
                medicion.getTxPower());

        Log.d(ETIQUETA_LOG, " guardarMedicion(): JSON=" + cuerpoJSON);
        Log.d(ETIQUETA_LOG, " guardarMedicion(): URL=" + URL_SERVIDOR);

        // ---- 3. Enviarlo con el cliente HTTP que ya existia ---------------------
        //         Un unico POST. El callback "laRespuesta" es el que recibe el
        //         codigo y el cuerpo de la respuesta, mas adelante y en otro hilo.
        //
        //         El cliente es NUEVO en cada llamada: PeticionarioREST es un
        //         AsyncTask y solo admite una ejecucion por instancia. Reusar la
        //         misma (que es lo que hacia antes) hacia fallar la app en el
        //         segundo POST con "the task is already running".
        Log.d(ETIQUETA_LOG, " guardarMedicion(): envio el POST ...");

        PeticionarioREST elClienteDeEstaVez = ( this.elCliente != null )
                ? this.elCliente
                : new PeticionarioREST();

        elClienteDeEstaVez.hacerPeticionREST("POST", URL_SERVIDOR, cuerpoJSON, laRespuesta);

        // ---- 4. Log final: aqui solo se ha enviado; lo que conteste el servidor
        //         llega despues en "laRespuesta", no ahora.
        Log.d(ETIQUETA_LOG, " guardarMedicion(): POST enviado, esperando la respuesta");

    } // guardarMedicion()

    // ------------------------------------------------------------------
    //  construirJSON()
    //
    //  PROPOSITO : montar, en un unico String, el JSON que espera el servidor
    //              a partir de los 5 datos del beacon, ya transformados.
    //  PARAM.    : nombre_emisora, uuid, elMajor, elMinor, elTxPower (ya enteros).
    //  RETORNA   : String con el JSON.
    //  NOTA      : los numeros van SIN comillas, para que sean numeros y no
    //              texto. protected para que se pueda probar sin salir a la red.
    // ------------------------------------------------------------------
    protected String construirJSON(String nombre_emisora,
                                   String uuid,
                                   int elMajor,
                                   int elMinor,
                                   int elTxPower) {

        return "{ \"minor\": " + elMinor +
                ", \"uuid\": \"" + uuid + "\"" +
                ", \"major\": " + elMajor +
                ", \"tx_power\": " + elTxPower +
                ", \"nombre_emisora\": \"" + nombre_emisora + "\" }";

    } // construirJSON()

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
