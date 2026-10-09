// =====================================================================
// FICHERO : logica_fake.js
// PROPOSITO: Logica fake del NAVEGADOR. Hace la peticion HTTP al servidor
//            REST para recuperar las mediciones y devuelve los datos
//            LIMPIOS, listos para que la UX (ux.js) los pinte.
// USO      : Sprint 0 - Proyecto Beacon. La web pide GET /mediciones al
//            backend y muestra la lista de mediciones de la placa BLE.
// REGLA    : Esta logica NO pinta nada: solo obtiene y limpia datos.
//            La peticion se hace directamente con "fetch", no hay
//            cliente REST aparte.
// =====================================================================

// Objeto LogicaFake: la logica fake de la web (un solo objeto).
var LogicaFake = {

    // ---- URL del recurso de mediciones en el servidor REST ----
    // El proyecto se sirve dentro de htdocs de XAMPP como
    // /Proyecto_Sprint0 (junction), y el backend cuelga de
    // /Proyecto_Sprint0/backend/public. La web se sirve desde
    // /Proyecto_Sprint0/web, asi que esta ruta relativa funciona
    // porque la pagina y la API estan en el mismo servidor (sin CORS).
    URL_MEDICIONES: '/Proyecto_Sprint0/backend/public/mediciones',

    // =================================================================
    // Funcion: recuperarMedicion()
    // PROPOSITO: Pide al servidor TODAS las mediciones con un GET y
    //            devuelve el array de Mediciones ya limpio, con los
    //            numeros como Number y los textos como cadena.
    //            Nombre singular (recuperarMedicion) segun el diseno,
    //            aunque lo que devuelve es la lista completa.
    // PARAM.   : ninguno.
    // RETORNA  : Promise<Array> - lista de Mediciones.
    //            Rechaza con un mensaje en espanol si el servidor
    //            falla o si la respuesta no trae una lista.
    // () ----------------------------------------------------------------
    recuperarMedicion: async function () {
        console.log('>>>> [LogicaFake] pido las mediciones al servidor:', this.URL_MEDICIONES);

        // ---- 1. Hacemos el GET al servidor REST ----
        // Se espera "GET" con "cache: no-store" para que el navegador
        // no sirva una copia vieja: la web se refresca cada 5 segundos.
        const res = await fetch(this.URL_MEDICIONES, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            cache: 'no-store'
        });

        // ---- 2. Si el servidor responde con error, rechazamos ----
        // Intentamos usar el {error} que devuelve el backend; si el
        // cuerpo no es JSON valido usamos un mensaje en espanol.
        if (!res.ok) {
            let mensaje = 'el servidor haFallado con el codigo ' + res.status;
            try {
                const datosError = await res.json();
                if (datosError && datosError.error) {
                    mensaje = datosError.error;
                }
            } catch (e) {
                console.log('>>>> [LogicaFake] el cuerpo del error no es JSON');
            }
            console.log('>>>> [LogicaFake] error del servidor:', mensaje);
            throw new Error(mensaje);
        }

        // ---- 3. Parseamos la respuesta y sacamos la lista ----
        const datos = await res.json();

        // Si "mediciones" no es un array, la respuesta no sirve.
        if (!datos || !Array.isArray(datos.mediciones)) {
            console.log('>>>> [LogicaFake] la respuesta no trae una lista de mediciones');
            throw new Error('la respuesta del servidor no contiene una lista de mediciones');
        }

        // ---- 4. Limpiamos cada Medicion ----
        // valor a Number (la BBDD puede devolverlo como texto);
        // tipo_medicion, nombre_emisora y fecha_hora a cadena.
        const mediciones = datos.mediciones.map(function (m) {
            return {
                id:             m.id,
                tipo_medicion:  m.tipo_medicion === null || m.tipo_medicion === undefined
                                    ? null : String(m.tipo_medicion),
                valor:          m.valor === null || m.valor === undefined
                                    ? null : Number(m.valor),
                nombre_emisora: m.nombre_emisora === null || m.nombre_emisora === undefined
                                    ? null : String(m.nombre_emisora),
                fecha_hora:     m.fecha_hora === null || m.fecha_hora === undefined
                                    ? null : String(m.fecha_hora)
            };
        });

        console.log('>>>> [LogicaFake] he recibido', mediciones.length, 'mediciones');
        return mediciones;
    }
};

// () Fin de LogicaFake.

// ---- Exportamos el objeto para poder probarlo desde Node ----
// En el navegador "module" no existe, asi que esta linea no molesta.
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LogicaFake;
}