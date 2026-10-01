package com.example.mmarvid1.beaconapp;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mmarvid1.beaconapp.logicafake.LogicaFake;

import java.util.List;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // --------------------------------------------------------------
    //  Longitud minima de una trama iBeacon:
    //  3 (flags) + 2 (advHeader) + 2 (companyID) + 1 (tipo)
    //  + 1 (longitud) + 16 (uuid) + 2 (major) + 2 (minor) + 1 (txPower)
    //  = 30 bytes.
    // --------------------------------------------------------------
    private static final int LONGITUD_MINIMA_TRAMA_IBEACON = 30;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    //  laLogicaFake: LogicaFake
    //
    //  La que sabe mandar la medicion al servidor REST por POST.
    // --------------------------------------------------------------
    private LogicaFake laLogicaFake = new LogicaFake();

    // --------------------------------------------------------------
    //  Respuesta del servidor. Ademas de mirar el codigo, DESTAPA el
    //  semaforo de "peticion en curso", que mientras siga puesto impide
    //  encolar mas POSTs.
    // --------------------------------------------------------------
    private PeticionarioREST.RespuestaREST respuestaDelServidor =
            new PeticionarioREST.RespuestaREST() {
                @Override
                public void callback(int codigo, String cuerpo) {
                    Log.d(ETIQUETA_LOG, " MainActivity: el servidor REST ha contestado codigo = "
                            + codigo + " cuerpo = " + cuerpo);
                    peticionEnCurso = false;
                }
            };

    // --------------------------------------------------------------
    //  Momento del ultimo POST, para no mandar uno cada vez que se ve
    //  el beacon (el escaner entrega resultados MUCHAS veces por
    //  segundo).
    // --------------------------------------------------------------
    private long instanteDelUltimoPost = 0;

    // --------------------------------------------------------------
    //  Semaforo: true mientras hay un POST sin responder. Evita que se
    //  acumulen peticiones si el servidor va lento o no hay cobertura.
    // --------------------------------------------------------------
    private boolean peticionEnCurso = false;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToIntOK(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToIntOK(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( "
                + Utilidades.bytesToIntOK(new byte[]{ tib.getTxPower() }) + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado ) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);

                String elNombre = nombreDelScanRecord( resultado );

                if ( ! elNombre.equals( dispositivoBuscado ) ) {
                    return; // no es nuestra emisora, me callo
                }

                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): es el beacon " + dispositivoBuscado );

                procesarMedicion( resultado, elNombre );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() codigo=" + errorCode );

            }
        };

        // ScanSettings: sin esto el escaner puede ir en modo de bajo consumo,
        // que NO pide el scan response, y entonces el nombre de la emisora
        // no llega nunca. LOW_LATENCY si lo pide.
        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado );

        // Sin ScanFilter a proposito: el filtro por nombre de Android no
        // siempre casa con el nombre que va en el scan response. Escanear
        // todo y descartar aqui es mas fiable.
        this.elEscanner.startScan( null, ajustes, this.callbackDelEscaneo );
    } // ()

    // --------------------------------------------------------------
    //  nombreDelScanRecord()
    //
    //  El nombre de la emisora va en el SCAN RESPONSE, asi que hay que
    //  leerlo de ahi y no de getDevice().getName() (que necesita
    //  BLUETOOTH_CONNECT y solo devuelve el nombre cacheado del GAP).
    // --------------------------------------------------------------
    private String nombreDelScanRecord( ScanResult resultado ) {

        if ( resultado.getScanRecord() == null ) {
            return "";
        }

        String elNombre = resultado.getScanRecord().getDeviceName();

        return ( elNombre == null ? "" : elNombre );
    } // ()

    // --------------------------------------------------------------
    //  procesarMedicion()
    //
    //  1. Parte los bytes de la trama.
    //  2. Construye la Medicion (que convierte los bytes a enteros).
    //  3. Se la pasa a LogicaFake, que hace el POST.
    // --------------------------------------------------------------
    private void procesarMedicion( ScanResult resultado, String elNombre ) {

        byte[] bytes = resultado.getScanRecord().getBytes();

        if ( bytes.length < LONGITUD_MINIMA_TRAMA_IBEACON ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): trama demasiado corta ("
                    + bytes.length + " bytes), la ignoro");
            return;
        }

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Medicion laMedicion = new Medicion( elNombre, tib );

        Log.d(ETIQUETA_LOG, " procesarMedicion(): " + laMedicion );

        // ---- no machacar el servidor: como mucho un POST cada X ms, y solo si
        //      no hay ya otro POST esperando respuesta ----
        long ahora = System.currentTimeMillis();

        if ( this.peticionEnCurso ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): ya hay un POST sin responder, no encolo otro");
            return;
        }

        if ( ahora - this.instanteDelUltimoPost < Constantes.INTERVALO_MINIMO_POST ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): todavia no toca mandar otro POST");
            return;
        }

        this.instanteDelUltimoPost = ahora;
        this.peticionEnCurso = true;

        this.laLogicaFake.guardarMedicion( laMedicion, respuestaDelServidor );

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );

        this.buscarEsteDispositivoBTLE( Constantes.NOMBRE_EMISORA );

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        pedirPermisosSiFaltan();

    } // ()

    // --------------------------------------------------------------
    //  pedirPermisosSiFaltan()
    //
    //  A partir de Android 6 los permisos se piden en tiempo de ejecucion.
    //  Y a partir de Android 12 el escaneo BLE necesita BLUETOOTH_SCAN
    //  (y BLUETOOTH_CONNECT para getDevice().getName()), no solo los
    //  permisos antiguos BLUETOOTH / BLUETOOTH_ADMIN, que ya no sirven
    //  para nada en un dispositivo moderno.
    // --------------------------------------------------------------
    private void pedirPermisosSiFaltan() {

        java.util.ArrayList<String> losQueFaltan = new java.util.ArrayList<String>();

        anadirPermisoSiFalta( losQueFaltan, Manifest.permission.ACCESS_FINE_LOCATION );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
            anadirPermisoSiFalta( losQueFaltan, Manifest.permission.BLUETOOTH_SCAN );
            anadirPermisoSiFalta( losQueFaltan, Manifest.permission.BLUETOOTH_CONNECT );
        } else {
            anadirPermisoSiFalta( losQueFaltan, Manifest.permission.BLUETOOTH );
            anadirPermisoSiFalta( losQueFaltan, Manifest.permission.BLUETOOTH_ADMIN );
        }

        if ( losQueFaltan.isEmpty() ) {
            Log.d(ETIQUETA_LOG, " pedirPermisosSiFaltan(): parece que YA tengo los permisos necesarios !!!!");
            return;
        }

        Log.d(ETIQUETA_LOG, " pedirPermisosSiFaltan(): me faltan " + losQueFaltan );

        ActivityCompat.requestPermissions(
                MainActivity.this,
                losQueFaltan.toArray( new String[0] ),
                CODIGO_PETICION_PERMISOS);

    } // ()

    // --------------------------------------------------------------
    private void anadirPermisoSiFalta( java.util.ArrayList<String> losQueFaltan,
                                       String elPermiso ) {

        if ( ContextCompat.checkSelfPermission(this, elPermiso)
                != PackageManager.PERMISSION_GRANTED ) {
            losQueFaltan.add( elPermiso );
        }

    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------

