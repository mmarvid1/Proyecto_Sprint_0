// =====================================================================
// FICHERO : integracion.test.js
// PROPOSITO: Prueba de integracion REAL contra el servidor REST de XAMPP.
//            No usa mocks: hace el mismo GET que hace la web y comprueba
//            que llegan mediciones de verdad con los campos correctos.
// USO      : Sprint 0 - Proyecto Beacon. Necesitas Apache y MySQL
//            encendidos (XAMPP) y la base de datos creada.
// Ejecutar  : node web/tests/integracion.test.js
// =====================================================================

const { comprobar, igual, cierto, contiene, titulo, resumen } = require('./ayuda.js');
const LogicaFake = require('../js/logica_fake.js');

titulo('integracion.test.js - GET real al servidor REST de XAMPP');

// En el navegador la URL "/Proyecto_Sprint0/..." es relativa y el
// navegador la resuelve sola contra el origen de la pagina. El fetch de
// Node NO acepta URLs relativas, asi que aqui le ponemos delante el
// origen. Se puede cambiar con la variable de entorno BEACON_ORIGEN
// (por ejemplo, para probar contra la IP del telefono).
const ORIGEN = process.env.BEACON_ORIGEN || 'http://localhost';
const URL_MEDICIONES = ORIGEN + LogicaFake.URL_MEDICIONES;

comprobar('el servidor devuelve la lista de mediciones con los 5 campos', async function () {
    console.log('  (probando contra ' + URL_MEDICIONES + ')');

    // ---- 1. Llamamos a la logica fake de verdad (sin mocks) ----
    // LogicaFake usa la URL relativa, que aqui no vale: hacemos el GET
    // a mano contra la URL absoluta, que es lo mismo que hace la web.
    const res = await fetch(URL_MEDICIONES, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        cache: 'no-store'
    });
    igual(res.status, 200, 'el GET responde 200');

    const datos = await res.json();
    cierto(Array.isArray(datos.mediciones), 'la respuesta trae una lista');
    const mediciones = datos.mediciones;

    // ---- 2. Tiene que haber al menos la medicion guardada por POST ----
    cierto(Array.isArray(mediciones), 'devuelve un array');
    cierto(mediciones.length > 0, 'hay al menos una medicion en la base de datos');

    // ---- 3. Cada medicion trae los 5 campos del diseño ----
    for (const m of mediciones) {
        for (const campo of ['id', 'tipo_medicion', 'valor',
                             'nombre_emisora', 'fecha_hora']) {
            cierto(campo in m, 'la medicion ' + m.id + ' trae el campo ' + campo);
        }
        // El valor medido es el campo "valor" y llega como numero.
        igual(typeof m.valor, 'number', 'valor es un numero');
        cierto(Number.isInteger(m.valor), 'valor es un entero');
    }

    // ---- 4. Esta ordenada de la mas reciente a la mas antigua ----
    const fechas = mediciones.map(function (m) { return m.fecha_hora; });
    const ordenadas = fechas.slice().sort().reverse();
    igual(JSON.stringify(fechas), JSON.stringify(ordenadas),
          'las mediciones llegan ordenadas de mas reciente a mas antigua');
});

comprobar('la ultima medicion guardada por POST es la primera de la lista', async function () {
    const url = URL_MEDICIONES;

    // ---- 1. Guardamos una medicion por POST, como haria Android ----
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            tipo_medicion: 'MANUAL',
            valor: 1234,
            nombre_emisora: 'Minerva_ELBACON'
        })
    });

    igual(res.status, 200, 'el POST responde 200');
    const guardado = await res.json();
    igual(guardado.ok, true, 'la respuesta trae ok:true');
    cierto(Number.isInteger(guardado.id), 'la respuesta trae el id numerico');

    // ---- 2. Ahora la recuperamos y debe ser la primera fila ----
    const res2 = await fetch(URL_MEDICIONES, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        cache: 'no-store'
    });
    const mediciones = (await res2.json()).mediciones;
    const primera = mediciones[0];

    igual(primera.id, guardado.id, 'la medicion guardada es la primera de la lista');
    igual(primera.valor, 1234, 'el valor medido es 1234');
    igual(primera.tipo_medicion, 'MANUAL', 'el tipo_medicion se guardo bien');
    igual(primera.nombre_emisora, 'Minerva_ELBACON', 'la emisora se guardo bien');
    cierto(primera.fecha_hora !== null && primera.fecha_hora !== '',
           'la BBDD puso la fecha_hora');
    contiene(primera.fecha_hora, '-');   // formato aaaa-mm-dd hh:mm:ss
});

resumen('integracion.test.js');