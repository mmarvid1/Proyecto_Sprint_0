package com.example.mmarvid1.beaconapp;

import java.util.Arrays;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   TramaIBeacon
// -----------------------------------------------------------------------------------
//                          --------- TramaIBeacon ---------
//                          |
//                          |
//                          |
//                          | prefijo: [Byte]_9
//                          | uuid: [Byte]_16
//                          | major: [Byte]_2
//                          | minor: [Byte]_2
//                          | txPower: Byte
//                          | losBytes: [Byte]
//                          | advFlags: [Byte]_3
//                          | advHeader: [Byte]_2
//                          | companyID: [Byte]_2
//                          | iBeaconType: Byte
//                          | iBeaconLength: Byte
//                          |
//                          |
//         bytes: [Byte] --> TramaIBeacon() -->
//                          |
//                          |
//     prefijo: [Byte]_9 <-- getPrefijo() <--
//                          |
//                          |
//       uuid: [Byte]_16 <-- getUUID() <--
//                          |
//                          |
//       major: [Byte]_2 <-- getMajor() <--
//                          |
//                          |
//       minor: [Byte]_2 <-- getMinor() <--
//                          |
//                          |
//         txPower: Byte <-- getTxPower() <--
//                          |
//                          |
//      losBytes: [Byte] <-- getLosBytes() <--
//                          |
//                          |
//    advFlags: [Byte]_3 <-- getAdvFlags() <--
//                          |
//                          |
//   advHeader: [Byte]_2 <-- getAdvHeader() <--
//                          |
//                          |
//   companyID: [Byte]_2 <-- getCompanyID() <--
//                          |
//                          |
//     iBeaconType: Byte <-- getiBeaconType() <--
//                          |
//                          |
//   iBeaconLength: Byte <-- getiBeaconLength() <--
//                          |
//                          --------------------------------------
// -----------------------------------------------------------------------------------

public class TramaIBeacon {
    private byte[] prefijo = null; // 9 bytes
    private byte[] uuid = null; // 16 bytes
    private byte[] major = null; // 2 bytes
    private byte[] minor = null; // 2 bytes
    private byte txPower = 0; // 1 byte

    private byte[] losBytes;

    private byte[] advFlags = null; // 3 bytes
    private byte[] advHeader = null; // 2 bytes
    private byte[] companyID = new byte[2]; // 2 bytes
    private byte iBeaconType = 0 ; // 1 byte
    private byte iBeaconLength = 0 ; // 1 byte

// -----------------------------------------------------------------------------------
//     prefijo: [Byte]_9 <-- getPrefijo() <--
// -----------------------------------------------------------------------------------
    public byte[] getPrefijo() {
        return prefijo;
    }

// -----------------------------------------------------------------------------------
//       uuid: [Byte]_16 <-- getUUID() <--
// -----------------------------------------------------------------------------------
    public byte[] getUUID() {
        return uuid;
    }

// -----------------------------------------------------------------------------------
//       major: [Byte]_2 <-- getMajor() <--
// -----------------------------------------------------------------------------------
    public byte[] getMajor() {
        return major;
    }

// -----------------------------------------------------------------------------------
//       minor: [Byte]_2 <-- getMinor() <--
// -----------------------------------------------------------------------------------
    public byte[] getMinor() {
        return minor;
    }

// -----------------------------------------------------------------------------------
//         txPower: Byte <-- getTxPower() <--
// -----------------------------------------------------------------------------------
    public byte getTxPower() {
        return txPower;
    }

// -----------------------------------------------------------------------------------
//      losBytes: [Byte] <-- getLosBytes() <--
// -----------------------------------------------------------------------------------
    public byte[] getLosBytes() {
        return losBytes;
    }

// -----------------------------------------------------------------------------------
//    advFlags: [Byte]_3 <-- getAdvFlags() <--
// -----------------------------------------------------------------------------------
    public byte[] getAdvFlags() {
        return advFlags;
    }

// -----------------------------------------------------------------------------------
//   advHeader: [Byte]_2 <-- getAdvHeader() <--
// -----------------------------------------------------------------------------------
    public byte[] getAdvHeader() {
        return advHeader;
    }

// -----------------------------------------------------------------------------------
//   companyID: [Byte]_2 <-- getCompanyID() <--
// -----------------------------------------------------------------------------------
    public byte[] getCompanyID() {
        return companyID;
    }

// -----------------------------------------------------------------------------------
//     iBeaconType: Byte <-- getiBeaconType() <--
// -----------------------------------------------------------------------------------
    public byte getiBeaconType() {
        return iBeaconType;
    }

// -----------------------------------------------------------------------------------
//   iBeaconLength: Byte <-- getiBeaconLength() <--
// -----------------------------------------------------------------------------------
    public byte getiBeaconLength() {
        return iBeaconLength;
    }

// -----------------------------------------------------------------------------------
//         bytes: [Byte] --> TramaIBeacon() -->
// -----------------------------------------------------------------------------------
    public TramaIBeacon(byte[] bytes ) {
        this.losBytes = bytes;

        prefijo = Arrays.copyOfRange(losBytes, 0, 8+1 ); // 9 bytes
        uuid = Arrays.copyOfRange(losBytes, 9, 24+1 ); // 16 bytes
        major = Arrays.copyOfRange(losBytes, 25, 26+1 ); // 2 bytes
        minor = Arrays.copyOfRange(losBytes, 27, 28+1 ); // 2 bytes
        txPower = losBytes[ 29 ]; // 1 byte

        advFlags = Arrays.copyOfRange( prefijo, 0, 2+1 ); // 3 bytes
        advHeader = Arrays.copyOfRange( prefijo, 3, 4+1 ); // 2 bytes
        companyID = Arrays.copyOfRange( prefijo, 5, 6+1 ); // 2 bytes
        iBeaconType = prefijo[ 7 ]; // 1 byte
        iBeaconLength = prefijo[ 8 ]; // 1 byte

    }
}