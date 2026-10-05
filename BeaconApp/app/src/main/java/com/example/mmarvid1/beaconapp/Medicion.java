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
//                        | uuid: Text
//                        | major: N
//                        | minor: N
//                        | txPower: Z
//                        |
//                        |
//  nombreEmisora: Text --> Medicion() -->
//          uuid: Text -->
//            major: N -->
//            minor: N -->
//          txPower: Z -->
//                        |
//                        |
//  nombreEmisora: Text --> Medicion() -->
//   tib: TramaIBeacon -->
//                        |
//                        |
//  nombreEmisora: Text <-- getNombreEmisora() <--
//                        |
//                        |
//          uuid: Text <-- getUuid() <--
//                        |
//                        |
//            major: N <-- getMajor() <--
//                        |
//                        |
//            minor: N <-- getMinor() <--
//                        |
//                        |
//          txPower: Z <-- getTxPower() <--
//                        |
//                        |
//      medicion: Text <-- toString() <--
//                        |
//                        --------------------------------------
// -----------------------------------------------------------------------------------

public class Medicion {
    private final String nombreEmisora;
    private final String uuid;
    private final int major;
    private final int minor;
    private final int txPower;

// -----------------------------------------------------------------------------------
// nombreEmisora: Text
//          uuid: Text
//            major: N
//            minor: N
//          txPower: Z
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String nombreEmisora, String uuid, int major, int minor, int txPower) {
        this.nombreEmisora = nombreEmisora;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
    }

// -----------------------------------------------------------------------------------
//  nombreEmisora: Text
//   tib: TramaIBeacon
//                  --> Medicion() -->
// -----------------------------------------------------------------------------------
    public Medicion(String nombreEmisora, TramaIBeacon tib) {
        this.nombreEmisora = nombreEmisora;
        this.uuid = Utilidades.bytesToString( tib.getUUID() );
        this.major = Utilidades.bytesToIntOK( tib.getMajor() );
        this.minor = Utilidades.bytesToIntOK( tib.getMinor() );
        this.txPower = Utilidades.bytesToIntOK( new byte[]{ tib.getTxPower() } );
    }

// -----------------------------------------------------------------------------------
//  nombreEmisora: Text <-- getNombreEmisora() <--
// -----------------------------------------------------------------------------------
    public String getNombreEmisora() {
        return this.nombreEmisora;
    }

// -----------------------------------------------------------------------------------
//          uuid: Text <-- getUuid() <--
// -----------------------------------------------------------------------------------
    public String getUuid() {
        return this.uuid;
    }

// -----------------------------------------------------------------------------------
//            major: N <-- getMajor() <--
// -----------------------------------------------------------------------------------
    public int getMajor() {
        return this.major;
    }

// -----------------------------------------------------------------------------------
//            minor: N <-- getMinor() <--
// -----------------------------------------------------------------------------------
    public int getMinor() {
        return this.minor;
    }

// -----------------------------------------------------------------------------------
//          txPower: Z <-- getTxPower() <--
// -----------------------------------------------------------------------------------
    public int getTxPower() {
        return this.txPower;
    }

// -----------------------------------------------------------------------------------
//      medicion: Text <-- toString() <--
// -----------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "Medicion{" +
                "nombre_emisora='" + this.nombreEmisora + '\'' +
                ", uuid='" + this.uuid + '\'' +
                ", major=" + this.major +
                ", minor=" + this.minor +
                ", tx_power=" + this.txPower +
                '}';
    }

}