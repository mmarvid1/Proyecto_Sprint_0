
package com.example.mmarvid1.beaconapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;

// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================

// -----------------------------------------------------------------------------------
//  DISENO   --   PeticionarioREST
// -----------------------------------------------------------------------------------
//                               --------- PeticionarioREST -----
//                               |
//                               |
//                               |
//                               | TIEMPO_ESPERA_MS: N   (const)
//                               | elMetodo: Text
//                               | urlDestino: Text
//                               | elCuerpo: Text
//                               | laRespuesta: RespuestaREST
//                               | codigoRespuesta: Z
//                               | cuerpoRespuesta: Text
//                               |
//                               |
//               metodo: Text --> hacerPeticionREST() -->
//           urlDestino: Text -->
//               cuerpo: Text -->
//  laRespuesta: RespuestaREST -->
//                               |
//                               |
//                                PeticionarioREST() -->
//                               |
//                               |
//                          B <-- doInBackground() -->
//                               |
//                               |
//                 comoFue: B --> onPostExecute() -->
//                               |
//                               |
//                 comoFue: B --> onCancelled() -->
//                               |
//                               --------------------------------------
//
//                               --------- RespuestaREST --------
//                               |
//                               |
//                               | // Interfaz anidada. La implementa MainActivity.
//                               |
//                               |
//                               |
//                  codigo: Z --> callback() -->
//               cuerpo: Text -->
//                               |
//                               --------------------------------------
// -----------------------------------------------------------------------------------

public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {
    private static final int TIEMPO_ESPERA_MS = 5000;
    public interface RespuestaREST {

// -----------------------------------------------------------------------------------
//                  codigo: Z
//               cuerpo: Text
//                          --> callback() -->
// -----------------------------------------------------------------------------------
        void callback (int codigo, String cuerpo);
    }

    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

// -----------------------------------------------------------------------------------
//               metodo: Text
//           urlDestino: Text
//               cuerpo: Text
//  laRespuesta: RespuestaREST
//                          --> hacerPeticionREST() -->
// -----------------------------------------------------------------------------------
    public void hacerPeticionREST (String metodo, String urlDestino, String cuerpo, RespuestaREST  laRespuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = laRespuesta;

        this.execute();
    }

// -----------------------------------------------------------------------------------
//                                PeticionarioREST() -->
// -----------------------------------------------------------------------------------
    public PeticionarioREST() {
        Log.d("clienterestandroid", "constructor()");
    }

// -----------------------------------------------------------------------------------
//                          B <-- doInBackground() -->
// -----------------------------------------------------------------------------------
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {

            // envio la peticion
            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            URL url = new URL(urlDestino);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty( "Content-Type", "application/json; charset-utf-8" );
            connection.setRequestMethod(this.elMetodo);

            connection.setDoInput(true);

            // ---- Tiempos maximos de espera ----
            // Sin esto, si el servidor no esta accesible, la llamada se queda colgada para siempre
            // y el callback no llega nunca.
            connection.setConnectTimeout(TIEMPO_ESPERA_MS);
            connection.setReadTimeout(TIEMPO_ESPERA_MS);

            if ( ! this.elMetodo.equals("GET") && this.elCuerpo != null ) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                connection.setDoOutput(true);
                // si no es GET, pongo el cuerpo que me den en la peticion
                DataOutputStream dos = new DataOutputStream (connection.getOutputStream());
                dos.writeBytes(this.elCuerpo);
                dos.flush();
                dos.close();
            }

            // ya he enviado la peticion
            Log.d("clienterestandroid", "doInBackground(): peticin enviada ");

            // ahora obtengo la respuesta
            int rc = connection.getResponseCode();
            String rm = connection.getResponseMessage();
            String respuesta = "" + rc + " : " + rm;
            Log.d("clienterestandroid", "doInBackground() recibo respuesta = " + respuesta);
            this.codigoRespuesta = rc;

try {

                // ---- El cuerpo hay que leerlo del "input stream" en un
                //      200, pero del "error stream" en un 4xx/5xx: si
                //      solo miramos el primero, getInputStream() lanza
                //      IOException y nos quedamos sin el mensaje de error
                //      que devuelve el servidor (por ejemplo el
                //      {"error":"falta el campo 'valor'"}).
                InputStream is = ( rc >= 200 && rc < 300 )
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                if ( is == null ) {
                    Log.d("clienterestandroid", "doInBackground(): la respuesta no tiene cuerpo");
                } else {

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    Log.d("clienterestandroid", "leyendo cuerpo");
                    StringBuilder acumulador = new StringBuilder ();
                    String linea;
                    while ( (linea = br.readLine()) != null ) {
                        Log.d("clienterestandroid", linea);
                        acumulador.append(linea);
                    }
                    Log.d("clienterestandroid", "FIN leyendo cuerpo");

                    this.cuerpoRespuesta = acumulador.toString();
                }

                Log.d("clienterestandroid", "cuerpo recibido=" + this.cuerpoRespuesta);

            } catch (IOException ex) {
                // salta si la respuesta no se puede leer de ninguna manera
                Log.d("clienterestandroid", "doInBackground(): no se ha podido leer el cuerpo: " + ex);
            } finally {
                // ---- Cerrar siempre la conexion ----
                connection.disconnect();
            }

            return true;

        } catch (Exception ex) {
            //  Aqui SOLO se llega si no hemos podido ni pedir la respuesta: servidor caido,
            //  IP equivocada, sin cobertura,ncortafuegos... El codigo se queda en 0 y el cuerpo vacio,
            //  que es como se le dice a MainActivity que no hubo respuesta.
            this.codigoRespuesta = 0;
            this.cuerpoRespuesta = "";
            Log.d("clienterestandroid", "doInBackground(): NO se ha podido comunicar con el "
                    + "servidor = " + urlDestino + " -> " + ex );
        }

        return false;
    }

// -----------------------------------------------------------------------------------
//                 comoFue: B --> onPostExecute() -->
// -----------------------------------------------------------------------------------
    protected void onPostExecute(Boolean comoFue) {
        // llamado tras doInBackground()
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
    }

// -----------------------------------------------------------------------------------
//                 comoFue: B --> onCancelled() -->
// -----------------------------------------------------------------------------------
    @Override
    protected void onCancelled(Boolean comoFue) {
        Log.d("clienterestandroid", "onCancelled(): la peticion se ha cancelado");
        this.codigoRespuesta = -1;
        this.cuerpoRespuesta = "";
        this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
    }
}