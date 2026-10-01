package com.example.mmarvid1.beaconapp;

// -----------------------------------------------------------------------------------
//  CLASE: Medicion
//
//  Que hace: guarda los 5 datos de una lectura del beacon (nombre, uuid, major,
//            minor y txPower) ya convertidos a numeros enteros, y sabe construirse
//            directamente desde los bytes de la trama.
//
//  La conversion de bytes a entero la pone Utilidades.bytesToIntOK(), que ya
//  existia en el proyecto. Aqui no se reimplementa, solo se le llama.
//
//  Ojo con por que hace falta esa conversion y no basta con mirar los bytes:
//  el "major" y el "minor" ocupan 2 bytes cada uno y el "txPower" 1 byte, y hay
//  que juntarlos para formar el numero.
//
//      el Arduino pone minor = 1234 (= 0x04D2)
//      en el aire viajan los bytes  0x04 0xD2
//
//  OJO: en ESTE proyecto los bytes van en orden normal, porque la libreria
//  BLEBeacon de Adafruit los cambia a proposito (en su constructor hace
//  __swap16). Por eso NO se pueden invertir al leer, que es lo que haria falta
//  con un iBeacon que los mandase al reves.
//
//  El UUID no hay que convertirlo: son 16 bytes que son directamente los 16
//  caracteres de texto, y Utilidades.bytesToString() los pasa a String tal cual.
// -----------------------------------------------------------------------------------
public class Medicion {

    // ------------------------------------------------------------------
    //  Atributos. Todos finales: una medicion no se modifica una vez creada.
    // ------------------------------------------------------------------
    private final String nombreEmisora;
    private final String uuid;
    private final int major;
    private final int minor;
    private final int txPower;

    // ------------------------------------------------------------------
    //  Constructor con los valores ya en enteros.
    //
    //  PROPOSITO : crear la medicion desde los datos que ya sabe el llamante.
    //  PARAM.    : nombreEmisora -> texto del beacon.
    //              uuid           -> texto del identificador del beacon.
    //              major          -> entero de 0 a 65535.
    //              minor          -> entero de 0 a 65535.
    //              txPower        -> entero CON signo (suele ser negativo).
    //  RETORNA   : nada (es un constructor).
    // ------------------------------------------------------------------
    public Medicion(String nombreEmisora, String uuid, int major, int minor, int txPower) {
        this.nombreEmisora = nombreEmisora;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
    } // ()

    // ------------------------------------------------------------------
    //  Constructor desde la trama.
    //
    //  PROPOSITO : construir la medicion a partir de la trama iBeacon leida del
    //              beacon, llamando a Utilidades para pasar los bytes a enteros.
    //  PARAM.    : nombreEmisora -> texto del beacon (viene del scan response).
    //              tib           -> la trama ya partida en sus campos.
    //  RETORNA   : nada (es un constructor).
    //  NOTA      : el UUID solo se pasa de bytes a texto. El major, el minor y el
    //              txPower si que necesitan la conversion numerica.
    // ------------------------------------------------------------------
    public Medicion(String nombreEmisora, TramaIBeacon tib) {
        this.nombreEmisora = nombreEmisora;
        this.uuid = Utilidades.bytesToString( tib.getUUID() );
        this.major = Utilidades.bytesToIntOK( tib.getMajor() );
        this.minor = Utilidades.bytesToIntOK( tib.getMinor() );
        this.txPower = Utilidades.bytesToIntOK( new byte[]{ tib.getTxPower() } );
    } // ()

    // ------------------------------------------------------------------
    public String getNombreEmisora() {
        return this.nombreEmisora;
    }

    // ------------------------------------------------------------------
    public String getUuid() {
        return this.uuid;
    }

    // ------------------------------------------------------------------
    public int getMajor() {
        return this.major;
    }

    // ------------------------------------------------------------------
    public int getMinor() {
        return this.minor;
    }

    // ------------------------------------------------------------------
    public int getTxPower() {
        return this.txPower;
    }

    // ------------------------------------------------------------------
    //  toString()
    //
    //  PROPOSITO : poder ver la medicion entera en un unico Log.d().
    // ------------------------------------------------------------------
    @Override
    public String toString() {
        return "Medicion{" +
                "nombre_emisora='" + this.nombreEmisora + '\'' +
                ", uuid='" + this.uuid + '\'' +
                ", major=" + this.major +
                ", minor=" + this.minor +
                ", tx_power=" + this.txPower +
                '}';
    } // toString()

} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
