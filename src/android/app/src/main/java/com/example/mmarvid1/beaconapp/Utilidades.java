package com.example.mmarvid1.beaconapp;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   Utilidades
// -----------------------------------------------------------------------------------
//                          --------- Utilidades -----------
//                          |
//                          |
//                          | // Clase de Conversion. Sin estado: todos sus
//                          | // metodos son estaticos (--x).
//                          |
//                          |
//                          |
//           texto: Text --> stringToBytes() --x
//                [Byte] <--
//                          |
//                          |
//            uuid: Text --> stringToUUID() --x
//                  UUID <--
//                          |
//                          |
//            uuid: UUID --> uuidToString() --x
//                  Text <--
//                          |
//                          |
//            uuid: UUID --> uuidToHexString() --x
//                  Text <--
//                          |
//                          |
//  masSignificativos: Z --> dosLongToBytes() --x
//  menosSignificativos: Z -->
//                [Byte] <--
//                          |
//                          |
//         bytes: [Byte] --> bytesToInt() --x
//                     Z <--
//                          |
//                          |
//         bytes: [Byte] --> bytesToLong() --x
//                     Z <--
//                          |
//                          |
//         bytes: [Byte] --> bytesToString() --x
//                  Text <--
//                          |
//                          |
//         bytes: [Byte] --> bytesToIntOK() --x
//                     Z <--
//                          |
//                          |
//         bytes: [Byte] --> bytesToHexString() --x
//                  Text <--
//                          |
//                          --------------------------------------
// -----------------------------------------------------------------------------------
public class Utilidades {

// -----------------------------------------------------------------------------------
//           texto: Text --> stringToBytes() --x
//                [Byte] <--
// -----------------------------------------------------------------------------------
    public static byte[] stringToBytes(String texto) {
        return texto.getBytes();
    }

// -----------------------------------------------------------------------------------
//            uuid: Text --> stringToUUID() --x
//                  UUID <--
// -----------------------------------------------------------------------------------
    public static UUID stringToUUID(String uuid) {
        if (uuid.length() != 16) {
            throw new Error("stringUUID: string no tiene 16 caracteres ");
        }
        byte[] comoBytes = uuid.getBytes();

        String masSignificativo = uuid.substring(0, 8);
        String menosSignificativo = uuid.substring(8, 16);
        UUID res = new UUID(Utilidades.bytesToLong(masSignificativo.getBytes()), Utilidades.bytesToLong(menosSignificativo.getBytes()));

        return res;
    }

// -----------------------------------------------------------------------------------
//            uuid: UUID --> uuidToString() --x
//                  Text <--
// -----------------------------------------------------------------------------------
    public static String uuidToString(UUID uuid) {
        return bytesToString(dosLongToBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
    }

// -----------------------------------------------------------------------------------
//            uuid: UUID --> uuidToHexString() --x
//                  Text <--
// -----------------------------------------------------------------------------------
    public static String uuidToHexString(UUID uuid) {
        return bytesToHexString(dosLongToBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> bytesToString() --x
//                  Text <--
// -----------------------------------------------------------------------------------
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }

// -----------------------------------------------------------------------------------
//  masSignificativos: Z
//  menosSignificativos: Z
//                       --> dosLongToBytes() --x
//                [Byte] <--
// -----------------------------------------------------------------------------------
    public static byte[] dosLongToBytes(long masSignificativos, long menosSignificativos) {
        ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
        buffer.putLong(masSignificativos);
        buffer.putLong(menosSignificativos);
        return buffer.array();
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> bytesToInt() --x
//                     Z <--
// -----------------------------------------------------------------------------------
    public static int bytesToInt(byte[] bytes) {
        return new BigInteger(bytes).intValue();
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> bytesToLong() --x
//                     Z <--
// -----------------------------------------------------------------------------------
    public static long bytesToLong(byte[] bytes) {
        return new BigInteger(bytes).longValue();
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> bytesToIntOK() --x
//                     Z <--
// -----------------------------------------------------------------------------------
    public static int bytesToIntOK(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }

        if (bytes.length > 4) {
            throw new Error("demasiados bytes para pasar a int ");
        }

        int res = 0;

        // ---- 1. De mas peso a menos peso: del PRIMER byte al ULTIMO ----
        for (int i = 0; i < bytes.length; i++) {
            res = (res << 8)  // corre lo que ya hay un byte a la izquierda
                    | (bytes[i] & 0xFF); // "& 0xFF" deja el byte sin signo (0..255)
        }

        // ---- 2. Si el byte mas significativo es negativo, restar su base ----
        int bits = 8 * bytes.length;

        if ((bytes[0] & 0x80) != 0) {
            res = res - (1 << bits);
        }

        return res;
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> bytesToHexString() --x
//                  Text <--
// -----------------------------------------------------------------------------------
    public static String bytesToHexString(byte[] bytes) {

        if (bytes == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(':');
        }
        return sb.toString();
    }
}