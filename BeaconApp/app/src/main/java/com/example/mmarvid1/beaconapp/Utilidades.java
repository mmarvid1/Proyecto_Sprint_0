package com.example.mmarvid1.beaconapp;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class Utilidades {

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static byte[] stringToBytes ( String texto ) {
        return texto.getBytes();
        // byte[] b = string.getBytes(StandardCharsets.UTF_8); // Ja
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static UUID stringToUUID( String uuid ) {
        if ( uuid.length() != 16 ) {
            throw new Error( "stringUUID: string no tiene 16 caracteres ");
        }
        byte[] comoBytes = uuid.getBytes();

        String masSignificativo = uuid.substring(0, 8);
        String menosSignificativo = uuid.substring(8, 16);
        UUID res = new UUID( Utilidades.bytesToLong( masSignificativo.getBytes() ), Utilidades.bytesToLong( menosSignificativo.getBytes() ) );

        // Log.d( MainActivity.ETIQUETA_LOG, " \n\n***** stringToUUID *** " + uuid  + "=?=" + Utilidades.uuidToString( res ) );

        // UUID res = UUID.nameUUIDFromBytes( comoBytes ); no va como quiero

        return res;
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static String uuidToString ( UUID uuid ) {
        return bytesToString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static String uuidToHexString ( UUID uuid ) {
        return bytesToHexString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static String bytesToString( byte[] bytes ) {
        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append( (char) b );
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static byte[] dosLongToBytes( long masSignificativos, long menosSignificativos ) {
        ByteBuffer buffer = ByteBuffer.allocate( 2 * Long.BYTES );
        buffer.putLong( masSignificativos );
        buffer.putLong( menosSignificativos );
        return buffer.array();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static int bytesToInt( byte[] bytes ) {
        return new BigInteger(bytes).intValue();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static long bytesToLong( byte[] bytes ) {
        return new BigInteger(bytes).longValue();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    //  bytesToIntOK(): convierte los bytes de un campo del iBeacon ("major", "minor"
    //  o "txPower") al entero que representan.
    //
    //  OJO CON EL ORDEN: se leen del PRIMER byte al ULTIMO, que es como los manda
    //  este proyecto. La libreria BLEBeacon de Adafruit, en su constructor, hace
    //  "_major_be = __swap16(major)" y "_minor_be = __swap16(minor)", o sea que
    //  cambia los bytes a proposito y en el aire quedan en ORDEN NORMAL. Por eso:
    //
    //      el Arduino pone minor = 1234 (= 0x04D2)
    //      en el aire viajan los bytes  0x04 0xD2
    //      y hay que leerlos tal cual, sin invertirlos
    //
    //  OJO CON EL SIGNO: un byte de Java va de -128 a 127, asi que hay que
    //  enmascarar con "& 0xFF" para que el 0xD2 cuente como 210 y no como -34.
    //  Despues, si el byte MAS SIGNIFICATIVO (el primero, que es el de mas peso)
    //  tiene el bit 7 puesto, el numero es negativo y hay que restarle
    //  2^(8*numBytes). Eso es justo lo que pasa con el "txPower", que es un solo
    //  byte con signo y suele valer cosas como -59.
    // -------------------------------------------------------------------------------
    public static int bytesToIntOK( byte[] bytes ) {

        if (bytes == null ) {
            return 0;
        }

        if ( bytes.length > 4 ) {
            throw new Error( "demasiados bytes para pasar a int ");
        }

        int res = 0;

        // ---- 1. De mas peso a menos peso: del PRIMER byte al ULTIMO ----
        for ( int i = 0; i < bytes.length; i++ ) {
            res = (res << 8)  // corre lo que ya hay un byte a la izquierda
                    | (bytes[i] & 0xFF); // "& 0xFF" deja el byte sin signo (0..255)
        }

        // ---- 2. Si el byte mas significativo es negativo, restar su base ----
        int bits = 8 * bytes.length;

        if ( ( bytes[ 0 ] & 0x80 ) != 0 ) {
            res = res - ( 1 << bits );
        }

        return res;
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static String bytesToHexString( byte[] bytes ) {

        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(':');
        }
        return sb.toString();
    } // ()
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------


