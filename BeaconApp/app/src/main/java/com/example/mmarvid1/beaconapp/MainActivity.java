package com.example.mmarvid1.beaconapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mmarvid1.beaconapp.logicafake.LogicaFake;

import java.util.List;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   MainActivity
// -----------------------------------------------------------------------------------
//                                --------- MainActivity ---------
//                                |
//                                |
//                                |
//                                | ETIQUETA_LOG: Text   (const)
//                                | NOMBRE_EMISORA: Text   (const)
//                                | INTERVALO_MINIMO_POST: N   (const)
//                                | CODIGO_PETICION_PERMISOS: N   (const)
//                                | CODIGO_PETICION_ENCENDER_BT: N   (const)
//                                | LONGITUD_MINIMA_TRAMA_IBEACON: N   (const)
//                                | elEscanner: BluetoothLeScanner
//                                | callbackDelEscaneo: ScanCallback
//                                | elAdaptadorBluetooth: BluetoothAdapter
//                                | receptorEstadoBluetooth: BroadcastReceiver
//                                | busquedaPendiente: Text
//                                | laLogicaFake: LogicaFake
//                                | respuestaDelServidor: RespuestaREST
//                                | instanteDelUltimoPost: N
//                                | peticionEnCurso: B
//                                |
//                                |
//                                 buscarTodosLosDispositivosBTLE() -->
//                                |
//                                |
//       resultado: ScanResult --> mostrarInformacionDispositivoBTLE() <--
//                                |
//                                |
//    dispositivoBuscado: Text --> buscarEsteDispositivoBTLE() -->
//                                |
//                                |
//       resultado: ScanResult --> nombreDelScanRecord() <--
//                nombre: Text <--
//                                |
//                                |
//       resultado: ScanResult --> procesarMedicion() -->
//              elNombre: Text -->
//                                |
//                                |
//                                 detenerBusquedaDispositivosBTLE() -->
//                                |
//                                |
//                     v: View --> botonBuscarDispositivosBTLEPulsado() -->
//                                |
//                                |
//                     v: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->
//                                |
//                                |
//                     v: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
//                                |
//                                |
//                                 inicializarBlueTooth() -->
//                                |
//                                |
//                                 registrarReceptorEstadoBluetooth() -->
//                                |
//                                |
//                           B <-- comprobarBluetoothYEscaner() -->
//                                |
//                                |
//                                 pedirEncenderBluetoothAlUsuario() -->
//                                |
//                                |
//                                 arrancarBusquedaPendiente() -->
//                                |
//                                |
//               mensaje: Text --> avisarAlUsuario() -->
//                                |
//                                |
//                                 pedirPermisosSiFaltan() -->
//                                |
//                                |
//        losQueFaltan: [Text] --> anadirPermisoSiFalta() -->
//             elPermiso: Text -->
//                                |
//                                |
//  savedInstanceState: Bundle --> onCreate() -->
//                                |
//                                |
//              requestCode: N --> onRequestPermissionsResult() -->
//         permissions: [Text] -->
//           grantResults: [Z] -->
//                                |
//                                |
//              requestCode: N --> onActivityResult() -->
//               resultCode: N -->
//                data: Intent -->
//                                |
//                                |
//                                 onDestroy() -->
//                                |
//                                --------------------------------------
//
//                                --------- ScanCallback (anonima) ---
//                                |
//                                |
//                                | // Clase anonima creada dentro de
//                                | // buscarTodosLosDispositivosBTLE() y de
//                                | // buscarEsteDispositivoBTLE(). El caso de
//                                | // buscarEsteDispositivoBTLE() es el que
//                                | // filtra por nombre y lanza procesarMedicion().
//                                |
//                                | dispositivoBuscado: Text
//                                |
//                                |
//             callbackType: N --> onScanResult() -->
//       resultado: ScanResult -->
//                                |
//                                |
//       results: [ScanResult] --> onBatchScanResults() <--
//                                |
//                                |
//                errorCode: N --> onScanFailed() <--
//                                |
//                                --------------------------------------
//
//                                --------- BroadcastReceiver (anonima) ---
//                                |
//                                |
//                                | // Clase anonima creada dentro de
//                                | // registrarReceptorEstadoBluetooth(). Avisa en
//                                | // cuanto el bluetooth se enciende.
//                                |
//                                |
//                                |
//           contexto: Context --> onReceive() -->
//           intencion: Intent -->
//                                |
//                                --------------------------------------
//
//                                --------- RespuestaREST (anonima) ---
//                                |
//                                |
//                                | // Clase anonima creada dentro del atributo
//                                | // respuestaDelServidor. Destapa el semaforo
//                                | // peticionEnCurso SIEMPRE, pase lo que pase.
//                                |
//                                |
//                                |
//                   codigo: Z --> callback() -->
//                cuerpo: Text -->
//                                |
//                                --------------------------------------
// -----------------------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";
    private static final String NOMBRE_EMISORA = "Minerva_ELBACON";

    //  Separacion minima entre dos POST seguidos, en milisegundos.
    private static final long INTERVALO_MINIMO_POST = 5000;
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private static final int CODIGO_PETICION_ENCENDER_BT = 55667788;

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
    private BluetoothAdapter elAdaptadorBluetooth = null;
    private BroadcastReceiver receptorEstadoBluetooth = null;


    //  En cuanto el bluetooth este listo, se arranca sola.
    private String busquedaPendiente = null;
    private LogicaFake laLogicaFake = new LogicaFake();

    // --------------------------------------------------------------
    //  Respuesta del servidor. Ademas de mirar el codigo, DESTAPA el
    //  semaforo de "peticion en curso", que mientras siga puesto impide
    //  encolar mas POSTs.
    // --------------------------------------------------------------
    private PeticionarioREST.RespuestaREST respuestaDelServidor =
            new PeticionarioREST.RespuestaREST() {

// -----------------------------------------------------------------------------------
//                   codigo: Z
//                cuerpo: Text
//                             --> callback() -->
// -----------------------------------------------------------------------------------
                @Override
                public void callback(int codigo, String cuerpo) {
                    Log.d(ETIQUETA_LOG, " MainActivity: el servidor REST ha contestado codigo = "
                            + codigo + " cuerpo = " + cuerpo);
                    // ---- Codigo 0 = no se pudo comunicar con el servidor;
                    //      -1 = la peticion se cancelo. En los dos casos no
                    //      hay nada guardado, asi que se avisa al usuario en
                    //      vez de dejarle creyendo que ha ido bien. ----
                    if ( codigo <= 0 ) {
                        avisarAlUsuario( "No se ha podido contactar con el servidor."
                                + " ¿Estáis en la misma red Wi-Fi?" );
                    }
                    // Destapamos el semaforo SIEMPRE, pase lo que pase: si
                    // no, la app deja de enviar mediciones para siempre.
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
    //  Semaforo: true mientras hay un POST sin responder. Evita que se acumulen peticiones.
    // --------------------------------------------------------------
    private boolean peticionEnCurso = false;

// -----------------------------------------------------------------------------------
//                                 buscarTodosLosDispositivosBTLE() -->
// -----------------------------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        // ---- Sin escaner no hay busqueda: no reventamos la app ----
        if ( ! comprobarBluetoothYEscaner() ) {
            Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): no hay escaner, busqueda pendiente");
            this.busquedaPendiente = "todos";
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {

// -----------------------------------------------------------------------------------
//             callbackType: N
//       resultado: ScanResult
//                          --> onScanResult() -->
// -----------------------------------------------------------------------------------
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

// -----------------------------------------------------------------------------------
//       results: [ScanResult] --> onBatchScanResults() <--
// -----------------------------------------------------------------------------------

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

// -----------------------------------------------------------------------------------
//                errorCode: N --> onScanFailed() <--
// -----------------------------------------------------------------------------------
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan( this.callbackDelEscaneo);

    }

// -----------------------------------------------------------------------------------
//       resultado: ScanResult --> mostrarInformacionDispositivoBTLE() <--
// -----------------------------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        int rssi = resultado.getRssi();

        // Sin scan record no hay trama que mirar ni nombre que sacar,
        // asi que no hay nada que mostrar de este resultado.
        if ( resultado.getScanRecord() == null ) {
            Log.d(ETIQUETA_LOG, " mostrarInformacionDispositivoBTLE(): este resultado no tiene "
                    + "scan record (rssi = " + rssi + "), lo ignoro" );
            return;
        }

        byte[] bytes = resultado.getScanRecord().getBytes();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + nombreDelScanRecord( resultado ));
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
    }

// -----------------------------------------------------------------------------------
//    dispositivoBuscado: Text --> buscarEsteDispositivoBTLE() -->
// -----------------------------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado ) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        // ---- Sin escaner no hay busqueda: no reventamos la app ----
        if ( ! comprobarBluetoothYEscaner() ) {
            Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): no hay escaner, busqueda pendiente");
            this.busquedaPendiente = dispositivoBuscado;
            return;
        }

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

        // Sin ScanFilter a proposito: el filtro por nombre de Android no siempre casa con el
        // nombre que va en el scan response. Escanear todo y descartar aqui es mas fiable.
        this.elEscanner.startScan( null, ajustes, this.callbackDelEscaneo );
    }

// -----------------------------------------------------------------------------------
//       resultado: ScanResult --> nombreDelScanRecord() <--
//                nombre: Text <--
//  El nombre de la emisora va en el SCAN RESPONSE, asi que hay que leerlo de ahi y no de
//  getDevice().getName() (que necesita BLUETOOTH_CONNECT y solo devuelve el nombre cacheado del GAP).
// -----------------------------------------------------------------------------------
    private String nombreDelScanRecord( ScanResult resultado ) {
        if ( resultado.getScanRecord() == null ) {
            return "";
        }

        String elNombre = resultado.getScanRecord().getDeviceName();

        return ( elNombre == null ? "" : elNombre );
    }

// -----------------------------------------------------------------------------------
//       resultado: ScanResult
//              elNombre: Text
//                          --> procesarMedicion() -->
// -----------------------------------------------------------------------------------
    private void procesarMedicion( ScanResult resultado, String elNombre ) {
        //  1. Parte los bytes de la trama.
        byte[] bytes = resultado.getScanRecord().getBytes();

        if ( bytes.length < LONGITUD_MINIMA_TRAMA_IBEACON ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): trama demasiado corta ("
                    + bytes.length + " bytes), la ignoro");
            return;
        }

        //  2. Construye la Medicion (que convierte los bytes a enteros).
        TramaIBeacon tib = new TramaIBeacon(bytes);

        Medicion laMedicion = new Medicion( elNombre, tib );

        Log.d(ETIQUETA_LOG, " procesarMedicion(): " + laMedicion );

        //  3. Se la pasa a LogicaFake, que hace el POST.

        // ---- no machacar el servidor: como mucho un POST cada X ms, y solo si
        //      no hay ya otro POST esperando respuesta ----
        long ahora = System.currentTimeMillis();

        if ( this.peticionEnCurso ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): ya hay un POST sin responder, no encolo otro");
            return;
        }

        if ( ahora - this.instanteDelUltimoPost < INTERVALO_MINIMO_POST ) {
            Log.d(ETIQUETA_LOG, " procesarMedicion(): todavia no toca mandar otro POST");
            return;
        }

        this.instanteDelUltimoPost = ahora;
        this.peticionEnCurso = true;

        this.laLogicaFake.guardarMedicion( laMedicion, respuestaDelServidor );
    }

// -----------------------------------------------------------------------------------
//                                 detenerBusquedaDispositivosBTLE() -->
// -----------------------------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {
        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        // ---- Sin escaner no hay nada que parar (y stopScan petaba) ----
        if ( this.elEscanner != null ) {
            this.elEscanner.stopScan( this.callbackDelEscaneo );
        }

        this.callbackDelEscaneo = null;

        this.busquedaPendiente = null;
    }

// -----------------------------------------------------------------------------------
//                     v: View --> botonBuscarDispositivosBTLEPulsado() -->
// -----------------------------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    }

// -----------------------------------------------------------------------------------
//                     v: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->
// -----------------------------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );

        this.buscarEsteDispositivoBTLE( NOMBRE_EMISORA );
    }

// -----------------------------------------------------------------------------------
//                     v: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
// -----------------------------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    }

// -----------------------------------------------------------------------------------
//        inicializarBlueTooth() -->
//
//  Solo deja el bluetooth listo: guarda el adaptador y se queda escuchando cuando se enciende.
// -----------------------------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        this.elAdaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();

        // 1. Nos quedamos escuchando a los cambios de estado
        // Asi, en cuanto el usuario encienda el bluetooth, lo sabemos
        // y podemos pedir el escaner.
        registrarReceptorEstadoBluetooth();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado = "
                + ( this.elAdaptadorBluetooth == null
                    ? "sin adaptador"
                    : this.elAdaptadorBluetooth.getState() ) );

        // 2. Pedimos los permisos (si no los_tuvieramos)
        pedirPermisosSiFaltan();

        // 3. Probamos a tener escaner ya
        comprobarBluetoothYEscaner();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): termina ");
    }

// -----------------------------------------------------------------------------------
//   registrarReceptorEstadoBluetooth() -->
//
//  Nos avisa de que el Bluetooth se acaba de encender
// -----------------------------------------------------------------------------------
    private void registrarReceptorEstadoBluetooth() {
        if ( this.receptorEstadoBluetooth != null ) {
            return;
        }

        this.receptorEstadoBluetooth = new BroadcastReceiver() {

// -----------------------------------------------------------------------------------
//  DISENO LOGICO   --   METODO
// -----------------------------------------------------------------------------------
//                                --------- MainActivity ---------
//                                |
//                                |
//                                |
//           contexto: Context --> onReceive() -->
//           intencion: Intent -->
//                                |
//                                --------------------------------------
// -----------------------------------------------------------------------------------
            @Override
            public void onReceive(Context contexto, Intent intencion) {

                String accion = ( intencion == null ? "" : intencion.getAction() );

                Log.d(ETIQUETA_LOG, " receptorEstadoBluetooth(): accion = " + accion );

                if ( BluetoothAdapter.ACTION_STATE_CHANGED.equals( accion ) ) {
                    comprobarBluetoothYEscaner();
                }
            }
        };

        // RegisterReceiver con los flags de la API 33+: el registro se
        // guarda hasta que se quite con unregisterReceiver.
        IntentFilter filtro = new IntentFilter( BluetoothAdapter.ACTION_STATE_CHANGED );

        ContextCompat.registerReceiver( this, this.receptorEstadoBluetooth, filtro,
                ContextCompat.RECEIVER_NOT_EXPORTED );

        Log.d(ETIQUETA_LOG, " registrarReceptorEstadoBluetooth(): receptor registrado ");
    }

// -----------------------------------------------------------------------------------
//       B <-- comprobarBluetoothYEscaner() -->
// -----------------------------------------------------------------------------------
    private boolean comprobarBluetoothYEscaner() {
        // 1. Sin adaptador no hay nada que hacer
        if ( this.elAdaptadorBluetooth == null ) {
            Log.d(ETIQUETA_LOG, " comprobarBluetoothYEscaner(): este movil NO tiene bluetooth");
            avisarAlUsuario( "Este móvil no tiene Bluetooth." );
            return false;
        }

        // 2. Comprobar si el Bluetooth está encendido
        int estado = this.elAdaptadorBluetooth.getState();

        Log.d(ETIQUETA_LOG, " comprobarBluetoothYEscaner(): estado del bluetooth = " + estado );

        if ( estado != BluetoothAdapter.STATE_ON ) {
            Log.d(ETIQUETA_LOG, " comprobarBluetoothYEscaner(): el bluetooth NO esta encendido, "
                    + "asi que no hay escaner todavia" );
            pedirEncenderBluetoothAlUsuario();

            return false;
        }

        // 3. Bluetooth encendido: pedimos el escaner
        this.elEscanner = this.elAdaptadorBluetooth.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " comprobarBluetoothYEscaner(): bluetooth encendido pero "
                    + "getBluetoothLeScanner() ha devuelto null (aun-calentando-se)" );
            return false;
        }

        Log.d(ETIQUETA_LOG, " comprobarBluetoothYEscaner(): TENEMOS escaner btle !!!! " );

        // 4. Si el usuario habia pulsado un boton antes, se arranca AHORA
        arrancarBusquedaPendiente();

        return true;
    }

// -----------------------------------------------------------------------------------
//        pedirEncenderBluetoothAlUsuario() -->
// -----------------------------------------------------------------------------------
    private void pedirEncenderBluetoothAlUsuario() {
        if ( this.elAdaptadorBluetooth != null
                && this.elAdaptadorBluetooth.isEnabled() ) {
            return;         // ya esta encendido, no molestamos
        }

        Log.d(ETIQUETA_LOG, " pedirEncenderBluetoothAlUsuario(): pedimos al usuario que lo encienda");

        try {
            startActivityForResult(
                    new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ),
                    CODIGO_PETICION_ENCENDER_BT );
        } catch (Exception e) {
            // Si el sistema no muestra el dialogo, avisamos y ya esta.
            Log.d(ETIQUETA_LOG, " pedirEncenderBluetoothAlUsuario(): no se ha podido pedir: " + e );
            avisarAlUsuario( "Enciende el Bluetooth y vuelve a pulsar el botón." );
        }
    }

// -----------------------------------------------------------------------------------
//             arrancarBusquedaPendiente() -->
//
//  Por si el usuario había pulsado el botón de buscar mientras el Bluetooth no estaba conectado.
// -----------------------------------------------------------------------------------
    private void arrancarBusquedaPendiente() {
        if ( this.busquedaPendiente == null ) {
            return;         // nadie ha pedido nada
        }

        String laBusqueda = this.busquedaPendiente;
        this.busquedaPendiente = null;

        Log.d(ETIQUETA_LOG, " arrancarBusquedaPendiente(): arrancamos la busqueda pendiente = "
                + laBusqueda );

        if ( "todos".equals( laBusqueda ) ) {
            buscarTodosLosDispositivosBTLE();
        } else {
            buscarEsteDispositivoBTLE( laBusqueda );
        }
    }

// -----------------------------------------------------------------------------------
//               mensaje: Text --> avisarAlUsuario() -->

// Muestra un mensaje breve en pantalla. Antes, si algo fallaba, bla app petaba y el usuario no entendia nada.
// -----------------------------------------------------------------------------------
    private void avisarAlUsuario( String mensaje ) {
        Log.d(ETIQUETA_LOG, " avisarAlUsuario(): " + mensaje );
        Toast.makeText( this, mensaje, Toast.LENGTH_LONG ).show();
    }

// -----------------------------------------------------------------------------------
//                       pedirPermisosSiFaltan() -->
//
// A partir de Android 6 los permisos se piden en tiempo de ejecucion. Y a partir de Android 12 el escaneo
// BLE necesita BLUETOOTH_SCAN (y BLUETOOTH_CONNECT para getDevice().getName()), no solo los
// permisos antiguos BLUETOOTH / BLUETOOTH_ADMIN
// -----------------------------------------------------------------------------------
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
    }

// -----------------------------------------------------------------------------------
//        losQueFaltan: [Text]
//             elPermiso: Text
//                            --> anadirPermisoSiFalta() -->
// -----------------------------------------------------------------------------------
    private void anadirPermisoSiFalta( java.util.ArrayList<String> losQueFaltan, String elPermiso ) {
        if ( ContextCompat.checkSelfPermission(this, elPermiso)
                != PackageManager.PERMISSION_GRANTED ) {
            losQueFaltan.add( elPermiso );
        }
    }

// -----------------------------------------------------------------------------------
//  savedInstanceState: Bundle --> onCreate() -->
// -----------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    }

// -----------------------------------------------------------------------------------
//              requestCode: N
//         permissions: [Text]
//           grantResults: [Z]
//                            --> onRequestPermissionsResult() -->
// -----------------------------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:

                //  Comprobamos TODOS los permisos, no solo el primero:
                //  si falta el permiso de escaneo, startScan() lanzaria SecurityException.
                boolean concedidosTodos = ( grantResults.length > 0 );

                for ( int i = 0; i < grantResults.length; i++ ) {
                    if ( grantResults[i] != PackageManager.PERMISSION_GRANTED ) {
                        concedidosTodos = false;
                    }
                }

                if ( concedidosTodos ) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");

                    //  Al conceder los permisos hay que volver a pedir
                    //  el escaner: sin permiso de escaneo, el sistema no nos lo da.
                    comprobarBluetoothYEscaner();

                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos NO concedidos  !!!!");

                    avisarAlUsuario( "Sin permisos de Bluetooth no se puede buscar el beacon." );

                }
                return;
        }
    }

// -----------------------------------------------------------------------------------
//              requestCode: N
//               resultCode: N
//                data: Intent
//                            --> onActivityResult() -->
//  Recoge el resultado del dialogo con el que se le ha pedido al usuario que encienda el bluetooth.
// -----------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode != CODIGO_PETICION_ENCENDER_BT ) {
            return;
        }

        // RESULT_OK = el usuario ha encendido el bluetooth y ha aceptado.
        if ( resultCode == RESULT_OK ) {

            Log.d(ETIQUETA_LOG, " onActivityResult(): el usuario ha encendido el bluetooth");

            // ---- Ahora si, pedir el escaner y arrancar lo pendiente ----
            comprobarBluetoothYEscaner();

        } else {

            Log.d(ETIQUETA_LOG, " onActivityResult(): el usuario NO ha encendido el bluetooth");

            avisarAlUsuario( "Enciende el Bluetooth y vuelve a pulsar el botón." );
        }
    }

// -----------------------------------------------------------------------------------
//   onDestroy() -->
//   Hay que quitar el receptor del bluetooth y parar el escaneo, o la app deja el bluetooth encendido y "colgada" al salir.
// -----------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();

        detenerBusquedaDispositivosBTLE();

        if ( this.receptorEstadoBluetooth != null ) {
            try {
                unregisterReceiver( this.receptorEstadoBluetooth );
            } catch (Exception e) {
                Log.d(ETIQUETA_LOG, " onDestroy(): no se ha podido quitar el receptor: " + e );
            }
            this.receptorEstadoBluetooth = null;
        }
    }
}